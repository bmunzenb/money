package com.munzenberger.money.app

import com.munzenberger.money.app.model.TransactionCategory
import com.munzenberger.money.app.model.displayName
import com.munzenberger.money.app.model.getAllWithParent
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.app.property.asyncExecute
import com.munzenberger.money.app.property.asyncValue
import com.munzenberger.money.app.property.singleValue
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.Entry
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.TransactionDetail
import com.munzenberger.money.core.Transfer
import com.munzenberger.money.core.getDetails
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.core.isPositive
import com.munzenberger.money.sql.transaction
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Singles
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
import java.time.LocalDate

class EditTransactionViewModel : TransactionDetailEditor(), AutoCloseable {

    private val accounts = SimpleAsyncObjectProperty<List<Account>>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val types = FXCollections.observableArrayList<TransactionType>()
    private val typeDisabled = SimpleBooleanProperty(true)
    private val categories = SimpleAsyncObjectProperty<List<TransactionCategory>>()
    private val categoryDisabled = SimpleBooleanProperty(true)
    private val splitDisabled = SimpleBooleanProperty(true)
    private val amountDisabled = SimpleBooleanProperty(true)
    private val saveStatus = SimpleAsyncStatusProperty()
    private val notValid = SimpleBooleanProperty()
    private val transactionStatus = SimpleStringProperty()

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
    val saveStatusProperty: ReadOnlyAsyncStatusProperty = saveStatus
    val notValidProperty: ReadOnlyBooleanProperty = notValid
    val transactionStatusProperty: ReadOnlyStringProperty = transactionStatus

    private lateinit var database: MoneyDatabase
    private lateinit var transaction: Transaction
    private lateinit var details: List<TransactionDetail>

    private var transactionType: TransactionType?
        get() = selectedTypeProperty.value
        set(value) { selectedTypeProperty.value = value }

    // keeps track of the editor in the editors list that this form is bound to
    private var boundEditor: TransactionDetailEditor? = null

    private val editorsChangeListener = ListChangeListener<TransactionDetailEditor> { change ->
        change.list.apply {

            boundEditor?.selectedCategoryProperty?.unbindBidirectional(selectedCategoryProperty)
            boundEditor?.amountProperty?.unbindBidirectional(amountProperty)
            boundEditor = null

            when (size) {
                1 -> first().let { first ->
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

    private val editors = FXCollections.observableArrayList<TransactionDetailEditor>().apply {
        addListener(editorsChangeListener)
    }

    init {
        notValid.bind(selectedAccountProperty.isNull
                .or(selectedTypeProperty.isNull)
                .or(dateProperty.isNull)
                .or(selectedCategoryProperty.isNull)
                .or(amountProperty.isNull))
    }

    fun start(database: MoneyDatabase, transaction: Transaction) {

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

        accounts.asyncValue { Account.getAll(database).sortedBy { it.name } }

        dateProperty.value = transaction.date ?: LocalDate.now()

        selectedPayeeProperty.value = transaction.payee

        payees.asyncValue { Payee.getAll(database).sortedBy { it.name } }

        numberProperty.value = transaction.number

        memoProperty.value = transaction.memo

        transactionStatus.value = transaction.status?.displayName

        val singleCategories = categories.singleValue {

            val categories = mutableListOf<TransactionCategory>()

            categories += Category.getAllWithParent(database)
                    .map { TransactionCategory.Entry(it.category, it.parentName) }
                    .sortedBy { it.name }

            categories += Account.getAll(database)
                    .map { TransactionCategory.Transfer(it) }
                    .sortedBy { it.name }

            categories
        }

        val singleDetails = Single.fromCallable { transaction.getDetails(database) }
                .doOnSuccess { details = it }

        Singles.zip(singleCategories, singleDetails)
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe(
                        { (categories, details) -> onCategoriesAndDetails(categories, details) },
                        ::onError
                )
    }

    private fun onCategoriesAndDetails(categories: List<TransactionCategory>, details: List<TransactionDetail>) {

        // calculate the total transaction amount
        val total = details.fold(Money.ZERO) { acc, detail ->
            when (val a = detail.amount) {
                null -> acc
                else -> acc + a
            }
        }

        // determine the transaction type based on the sign of the total
        transactionType = when {
            total.isPositive -> types.find { it.variant == TransactionType.Variant.CREDIT }
            total.isNegative -> types.find { it.variant == TransactionType.Variant.DEBIT }
            else -> null
        }

        // map to a set of editable transaction details
        details.map { TransactionDetailEditor(it, categories, transactionType) }
                .let { editors.setAll(it) }

        // if there are no editors, create one
        if (editors.isEmpty()) {
            editors += TransactionDetailEditor()
        }

        typeDisabled.value = false
        splitDisabled.value = false
    }

    fun save() {

        saveStatus.asyncExecute {

            database.transaction { tx ->

                transaction.apply {
                    account = selectedAccountProperty.value
                    date = dateProperty.value
                    payee = selectedPayeeProperty.value
                    number = numberProperty.value
                    memo = memoProperty.value
                    save(tx)
                }

                val mutableDetails = details.toMutableList()

                editors.forEachIndexed { index, editor ->

                    when (val c = editor.category) {

                        is TransactionCategory.Transfer -> {

                            // if the underlying detail is a transfer, update it
                            val transfer = when (val d = editor.detail) {
                                is TransactionDetail.Transfer -> d.transfer.also {
                                    mutableDetails.remove(d)
                                }
                                else -> Transfer().apply {
                                    setTransaction(transaction)
                                }
                            }

                            transfer.apply {
                                this.amount = editor.amount?.forTransactionType(transactionType)
                                this.account = c.account
                                this.memo = editor.memo
                                this.orderInTransaction = index
                                save(tx)
                            }
                        }

                        is TransactionCategory.Entry -> {

                            // if the underlying detail is an entry, update it
                            val entry = when (val d = editor.detail) {
                                is TransactionDetail.Entry -> d.entry.also {
                                    mutableDetails.remove(d)
                                }
                                else -> Entry().apply {
                                    setTransaction(transaction)
                                }
                            }

                            entry.apply {
                                this.amount = editor.amount?.forTransactionType(transactionType)
                                this.category = c.category
                                this.memo = editor.memo
                                this.orderInTransaction = index
                                save(tx)
                            }
                        }

                        is TransactionCategory.Pending -> {

                            // if the underlying detail is an entry, update it
                            val entry = when (val d = editor.detail) {
                                is TransactionDetail.Entry -> d.entry.also {
                                    mutableDetails.remove(d)
                                }
                                else -> Entry().apply {
                                    setTransaction(transaction)
                                }
                            }

                            entry.apply {
                                this.amount = editor.amount?.forTransactionType(transactionType)
                                this.category = c.getCategory(tx, editor.amount?.isNegative, transactionType)
                                this.memo = editor.memo
                                this.orderInTransaction = index
                                save(tx)
                            }
                        }
                    }
                }

                // delete any transfers or entries that weren't updated
                mutableDetails.forEach {
                    when (it) {
                        is TransactionDetail.Transfer -> it.transfer.delete(tx)
                        is TransactionDetail.Entry -> it.entry.delete(tx)
                    }
                }
            }
        }
    }

    fun prepareSplit(block: (ObservableList<TransactionDetailEditor>, List<TransactionCategory>) -> Unit) {
        if (splitDisabled.value == false) {
            when (val c = categories.get()) {
                is AsyncObject.Complete -> block.invoke(editors, c.value)
            }
        }
    }

    override fun close() {
        // do nothing
    }

    private fun onError(error: Throwable) {
        // TODO refactor this to the controller
        // idea: create a "disabled" async object property that can be bound by the controller
        ErrorAlert(error).showAndWait()
    }
}
