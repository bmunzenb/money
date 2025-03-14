package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.Executors
import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.model.TransactionCategory
import com.munzenberger.money.app.model.displayName
import com.munzenberger.money.app.model.getAllWithParent
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.CategoryEntry
import com.munzenberger.money.core.Entry
import com.munzenberger.money.core.EntryIdentity
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.TransferEntry
import com.munzenberger.money.core.getEntries
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.core.isPositive
import com.munzenberger.money.sql.transaction
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.concurrent.Task
import java.time.LocalDate

class EditTransactionViewModel :
    TransactionEntryEditor(),
    AutoCloseable {
    private val accounts = SimpleAsyncObjectProperty<List<Account>>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val types = FXCollections.observableArrayList<TransactionType>()
    private val typeDisabled = SimpleBooleanProperty(true)
    private val categories = SimpleAsyncObjectProperty<List<TransactionCategory>>()
    private val categoryDisabled = SimpleBooleanProperty(true)
    private val splitDisabled = SimpleBooleanProperty(true)
    private val amountDisabled = SimpleBooleanProperty(true)
    private val notValid = SimpleBooleanProperty()
    private val transactionStatus = SimpleStringProperty()
    private val isOperationInProgress = SimpleBooleanProperty(false)

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<Account>> = accounts
    val selectedAccountProperty = SimpleObjectProperty<Account?>()
    val typesProperty: ReadOnlyListProperty<TransactionType> = SimpleListProperty(types)
    val typeDisabledProperty: ReadOnlyBooleanProperty = typeDisabled
    val selectedTypeProperty = SimpleObjectProperty<TransactionType>()
    val dateProperty = SimpleObjectProperty<LocalDate>()
    val numberProperty = SimpleStringProperty()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee?>()
    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<TransactionCategory>> = categories
    val categoryDisabledProperty: ReadOnlyBooleanProperty = categoryDisabled
    val splitDisabledProperty: ReadOnlyBooleanProperty = splitDisabled
    val amountDisabledProperty: ReadOnlyBooleanProperty = amountDisabled
    val notValidProperty: ReadOnlyBooleanProperty = notValid
    val transactionStatusProperty: ReadOnlyStringProperty = transactionStatus
    val isOperationInProgressProperty: ReadOnlyBooleanProperty = isOperationInProgress

    private lateinit var database: MoneyDatabase
    private lateinit var transaction: Transaction
    private lateinit var entries: List<Entry<out EntryIdentity>>

    private var transactionType: TransactionType?
        get() = selectedTypeProperty.value
        set(value) {
            selectedTypeProperty.value = value
        }

    // keeps track of the editor in the editors list that this form is bound to
    private var boundEditor: TransactionEntryEditor? = null

    private val editorsChangeListener =
        ListChangeListener<TransactionEntryEditor> { change ->
            change.list.apply {

                boundEditor?.selectedCategoryProperty?.unbindBidirectional(selectedCategoryProperty)
                boundEditor?.amountProperty?.unbindBidirectional(amountProperty)
                boundEditor = null

                when (size) {
                    1 ->
                        first().let { first ->
                            selectedCategoryProperty.bindBidirectional(first.selectedCategoryProperty)
                            amountProperty.bindBidirectional(first.amountProperty)
                            boundEditor = first
                            categoryDisabled.value = false
                            amountDisabled.value = false
                        }
                    else -> {
                        category = TransactionCategory.Split
                        amount = fold(Money.ZERO) { acc, t -> acc + t.amount!! }
                        categoryDisabled.value = true
                        amountDisabled.value = true
                    }
                }
            }
        }

    private val editors =
        FXCollections.observableArrayList<TransactionEntryEditor>().apply {
            addListener(editorsChangeListener)
        }

    init {
        notValid.bind(
            selectedAccountProperty.isNull
                .or(selectedTypeProperty.isNull)
                .or(dateProperty.isNull)
                .or(selectedCategoryProperty.isNull)
                .or(amountProperty.isNull),
        )
    }

    fun start(
        database: MoneyDatabase,
        transaction: Transaction,
        onError: (Throwable) -> Unit,
    ) {
        this.database = database
        this.transaction = transaction

        selectedAccountProperty.addListener { _, _, newValue ->

            val selectedVariant = transactionType?.variant

            when (newValue) {
                null -> types.clear()
                else -> types.setAll(TransactionType.getTypes(newValue.accountType!!))
            }

            transactionType = types.find { it.variant == selectedVariant }
        }

        selectedAccountProperty.value = transaction.account

        accounts.setValueAsync { Account.find(database).sortedBy { it.name } }

        dateProperty.value = transaction.date ?: LocalDate.now()

        selectedPayeeProperty.value = transaction.payee

        payees.setValueAsync { Payee.find(database).sortedBy { it.name } }

        numberProperty.value = transaction.number

        memoProperty.value = transaction.memo

        transactionStatus.value = transaction.status?.displayName

        val task =
            object : Task<Pair<List<TransactionCategory>, List<Entry<out EntryIdentity>>>>() {
                override fun running() {
                    categories.set(AsyncObject.Executing())
                }

                override fun call(): Pair<List<TransactionCategory>, List<Entry<out EntryIdentity>>> {
                    val categories =
                        Category
                            .getAllWithParent(database)
                            .map { TransactionCategory.CategoryType(it.category, it.parentName) }
                            .sortedBy { it.name }

                    val accounts =
                        Account
                            .find(database)
                            .map { TransactionCategory.TransferType(it) }
                            .sortedBy { it.name }

                    return (categories + accounts) to transaction.getEntries(database)
                }

                override fun succeeded() {
                    val (cats, ents) = value

                    categories.set(AsyncObject.Complete(cats))
                    entries = ents

                    onCategoriesAndEntries(cats, ents)
                }

                override fun failed() {
                    categories.set(AsyncObject.Error(exception))
                    onError.invoke(exception)
                }
            }

        Executors.SINGLE.execute(task)
    }

    private fun onCategoriesAndEntries(
        categories: List<TransactionCategory>,
        entries: List<Entry<out EntryIdentity>>,
    ) {
        // calculate the total transaction amount
        val total =
            entries.fold(Money.ZERO) { acc, detail ->
                when (val a = detail.amount) {
                    null -> acc
                    else -> acc + a
                }
            }

        // determine the transaction type based on the sign of the total
        transactionType =
            when {
                total.isPositive -> types.find { it.variant == TransactionType.Variant.CREDIT }
                total.isNegative -> types.find { it.variant == TransactionType.Variant.DEBIT }
                else -> null
            }

        // map to a set of editable transaction details
        entries
            .map { TransactionEntryEditor(it, categories, transactionType) }
            .let { editors.setAll(it) }

        // if there are no editors, create one
        if (editors.isEmpty()) {
            editors += TransactionEntryEditor()
        }

        typeDisabled.value = false
        splitDisabled.value = false
    }

    fun save(block: (Throwable?) -> Unit) {
        val task =
            object : Task<Unit>() {
                override fun call() {
                    database.transaction { tx ->

                        transaction.apply {
                            account = selectedAccountProperty.value
                            date = dateProperty.value
                            payee = selectedPayeeProperty.value
                            number = numberProperty.value
                            memo = memoProperty.value
                            save(tx)
                        }

                        val entriesToDelete = entries.toMutableList()

                        editors.forEachIndexed { index, editor ->

                            when (val c = editor.category) {
                                is TransactionCategory.TransferType -> {
                                    // if the underlying entry is a transfer, update it
                                    val transfer =
                                        when (val e = editor.entry) {
                                            is TransferEntry -> e.also { entriesToDelete.remove(e) }
                                            else -> TransferEntry().apply { setTransaction(transaction) }
                                        }

                                    transfer.apply {
                                        this.amount = editor.amount?.forTransactionType(transactionType)
                                        this.account = c.account
                                        this.memo = editor.memo
                                        this.orderInTransaction = index
                                        save(tx)
                                    }
                                }

                                is TransactionCategory.CategoryType -> {
                                    // if the underlying entry is a category, update it
                                    val categoryEntry =
                                        when (val e = editor.entry) {
                                            is CategoryEntry -> e.also { entriesToDelete.remove(e) }
                                            else -> CategoryEntry().apply { setTransaction(transaction) }
                                        }

                                    categoryEntry.apply {
                                        this.amount = editor.amount?.forTransactionType(transactionType)
                                        this.category = c.category
                                        this.memo = editor.memo
                                        this.orderInTransaction = index
                                        save(tx)
                                    }
                                }

                                is TransactionCategory.Pending -> {
                                    // if the underlying entry is a category, update it
                                    val categoryEntry =
                                        when (val e = editor.entry) {
                                            is CategoryEntry -> e.also { entriesToDelete.remove(e) }
                                            else -> CategoryEntry().apply { setTransaction(transaction) }
                                        }

                                    categoryEntry.apply {
                                        this.amount = editor.amount?.forTransactionType(transactionType)
                                        this.category = c.getCategory(tx, editor.amount?.isNegative, transactionType)
                                        this.memo = editor.memo
                                        this.orderInTransaction = index
                                        save(tx)
                                    }
                                }

                                else ->
                                    error("Invalid TransactionCategory type: ${c?.javaClass?.simpleName}")
                            }
                        }

                        // delete any entries that weren't updated
                        entriesToDelete.forEach { it.delete(tx) }
                    }
                }

                override fun succeeded() {
                    block.invoke(null)
                }

                override fun failed() {
                    block.invoke(exception)
                }
            }

        isOperationInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }

    fun prepareSplit(block: (ObservableList<TransactionEntryEditor>, List<TransactionCategory>) -> Unit) {
        if (splitDisabled.value == false) {
            when (val c = categories.get()) {
                is AsyncObject.Complete -> block.invoke(editors, c.value)
                else -> Unit // do nothing
            }
        }
    }

    override fun close() {
        // no resources to dispose of
    }
}
