package com.munzenberger.money.app

import com.munzenberger.money.app.model.DelayedCategory
import com.munzenberger.money.app.model.PendingCategory
import com.munzenberger.money.app.model.RealCategory
import com.munzenberger.money.app.model.getAssetsAndLiabilities
import com.munzenberger.money.app.model.observableGetTransfers
import com.munzenberger.money.app.model.toDate
import com.munzenberger.money.app.model.toLocalDate
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.Transfer
import com.munzenberger.money.core.rx.observableGetAll
import com.munzenberger.money.core.rx.observableTransaction
import com.munzenberger.money.core.rx.sortedBy
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import java.time.LocalDate

abstract class EditTransferBase {

    private val valid = SimpleBooleanProperty(false)

    val selectedCategoryProperty = SimpleObjectProperty<DelayedCategory>()
    val amountProperty = SimpleObjectProperty<Money>()
    val memoProperty = SimpleStringProperty()
    val validProperty: ReadOnlyBooleanProperty = valid

    init {
        valid.bind(selectedCategoryProperty.isNotNull.and(amountProperty.isNotNull))
    }

    var category: DelayedCategory?
        get() = selectedCategoryProperty.value
        set(value) { selectedCategoryProperty.value = value }

    var amount: Money?
        get() = amountProperty.value
        set(value) { amountProperty.value = value }

    var memo: String?
        get() = memoProperty.value
        set(value) { memoProperty.value = value }

    val isValid: Boolean
        get() = valid.value
}

class EditTransactionViewModel : EditTransferBase(), AutoCloseable {

    private val accounts = SimpleAsyncObjectProperty<List<Account>>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val types = FXCollections.observableArrayList<TransactionType>()
    private val typeDisabled = SimpleBooleanProperty(true)
    private val categories = SimpleAsyncObjectProperty<List<DelayedCategory>>()
    private val categoryDisabled = SimpleBooleanProperty(true)
    private val splitDisabled = SimpleBooleanProperty(true)
    private val amountDisabled = SimpleBooleanProperty(true)
    private val saveStatus = SimpleAsyncStatusProperty()
    private val notValid = SimpleBooleanProperty()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<Account>> = accounts
    val selectedAccountProperty = SimpleObjectProperty<Account?>()
    val typesProperty: ReadOnlyListProperty<TransactionType> = SimpleListProperty(types)
    val typeDisabledProperty: ReadOnlyBooleanProperty = typeDisabled
    val selectedTypeProperty = SimpleObjectProperty<TransactionType>()
    val dateProperty = SimpleObjectProperty<LocalDate>()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee?>()
    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<DelayedCategory>> = categories
    val categoryDisabledProperty: ReadOnlyBooleanProperty = categoryDisabled
    val splitDisabledProperty: ReadOnlyBooleanProperty = splitDisabled
    val amountDisabledProperty: ReadOnlyBooleanProperty = amountDisabled
    val saveStatusProperty: ReadOnlyAsyncStatusProperty = saveStatus
    val notValidProperty: ReadOnlyBooleanProperty = notValid

    private lateinit var database: MoneyDatabase
    private lateinit var transaction: Transaction
    private lateinit var transfers: List<Transfer>

    private var transactionType: TransactionType?
        get() = selectedTypeProperty.value
        set(value) { selectedTypeProperty.value = value }

    private val editTransfers: ObservableList<EditTransfer> = FXCollections.observableArrayList<EditTransfer>().apply {
        addListener(ListChangeListener {

            it.list.first().let { first ->
                selectedCategoryProperty.unbindBidirectional(first.selectedCategoryProperty)
                amountProperty.unbindBidirectional(first.amountProperty)
            }

            when (it.list.size){
                1 -> {
                    it.list.first().let { first ->
                        selectedCategoryProperty.bindBidirectional(first.selectedCategoryProperty)
                        amountProperty.bindBidirectional(first.amountProperty)
                    }
                    categoryDisabled.value = false
                    amountDisabled.value = false
                }
                else -> {
                    category = PendingCategory("Split")
                    amount = it.list.fold(Money.ZERO) { acc, t -> acc.add(t.amount!!) }
                    categoryDisabled.value = true
                    amountDisabled.value = true
                }
            }
        })
    }

    init {
        notValid.bind(selectedAccountProperty.isNull
                .or(selectedTypeProperty.isNull)
                .or(dateProperty.isNull)
                .or(validProperty.not()))
    }

    fun start(database: MoneyDatabase, transaction: Transaction, schedulers: SchedulerProvider = SchedulerProvider.Default) {

        this.database = database
        this.transaction = transaction

        selectedAccountProperty.addListener { _, _, newValue ->

            val selectedType = transactionType

            when (newValue) {
                null -> types.clear()
                else -> types.setAll(TransactionType.getTypes(newValue.accountType!!))
            }

            transactionType = types.find { it.variant == selectedType?.variant }
        }

        selectedAccountProperty.value = transaction.account

        accounts.subscribeTo(Account.getAssetsAndLiabilities(database))

        dateProperty.value = transaction.date?.toLocalDate() ?: LocalDate.now()

        selectedPayeeProperty.value = transaction.payee

        payees.subscribeTo(Payee.observableGetAll(database).sortedBy { it.name })

        categories.subscribeTo(Category.observableGetAll(database).map {
            it.map { c -> RealCategory(c) }
        })

        transaction.observableGetTransfers(database)
                .subscribeOn(schedulers.database)
                .observeOn(schedulers.main)
                .subscribe(::onTransfers)
    }

    private fun onTransfers(transfers: List<Transfer>) {

        this.transfers = when {
            // make sure there's at least one transfer
            transfers.isEmpty() -> listOf(Transfer().apply { setTransaction(transaction) })
            else -> transfers
        }

        val sum = transfers.fold(0L) { acc, t ->
            when (val a= t.amount) {
                null -> acc + 0L
                else -> acc + a
            }
        }

        transactionType = when {
            sum > 0 -> types.find { it.variant == TransactionType.Variant.CREDIT }
            sum < 0 -> types.find { it.variant == TransactionType.Variant.DEBIT }
            else -> null
        }

        editTransfers.setAll(this.transfers.map { EditTransfer.from(it, transactionType) })

        typeDisabled.value = false
        splitDisabled.value = false
    }

    fun save() {

        val save = database.observableTransaction { tx ->

            transaction.apply {
                account = selectedAccountProperty.value
                date = dateProperty.value.toDate()
                payee = selectedPayeeProperty.value
                memo = memoProperty.value
                save(tx)
            }

            editTransfers.forEachIndexed { index, edit ->

                // convert any pending categories into real categories
                when (val c = edit.category) {
                    is PendingCategory -> edit.category = c.toRealCategory(tx, transactionType!!)
                }

                val transfer = when {
                    // update existing transfer
                    index < transfers.size -> transfers[index]
                    // create new transfer
                    else -> Transfer().apply { setTransaction(transaction) }
                }

                transfer.apply {
                    this.amount = edit.getAmountValue(transactionType!!)
                    this.category = edit.realCategory
                    this.memo = edit.memo
                    save(tx)
                }
            }

            // delete any transfers in excess of the number updated/created
            transfers.drop(editTransfers.size).forEach {
                it.delete(tx)
            }
        }

        saveStatus.subscribeTo(save)
    }

    fun prepareSplit(block: (ObservableList<EditTransfer>, List<DelayedCategory>) -> Unit) {

        val c = categories.get()
        if (c is AsyncObject.Complete) {

            if (editTransfers.size == 1) {
                editTransfers.first().memo = memo
            }

            block.invoke(editTransfers, c.value)
        }
    }

    override fun close() {
        // do nothing
    }
}

class EditTransfer : EditTransferBase() {

    companion object {

        fun from(transfer: Transfer, transactionType: TransactionType?) = EditTransfer().apply {

            category = when (val c = transfer.category) {
                null -> null
                else -> RealCategory(c)
            }

            amount = transfer.amount?.let {
                val a= when (transactionType?.variant) {
                    TransactionType.Variant.DEBIT -> -it
                    else -> it
                }
                a.let { i -> Money.valueOf(i) }
            }

            memo = transfer.memo
        }

        fun from(editTransfer: EditTransfer) = EditTransfer().apply {
            category = editTransfer.category
            amount = editTransfer.amount
            memo = editTransfer.memo
        }
    }

    fun getAmountValue(transactionType: TransactionType): Long {
        return when (transactionType.variant) {
            TransactionType.Variant.CREDIT -> amount!!.value
            TransactionType.Variant.DEBIT -> -amount!!.value
        }
    }

    val realCategory: Category
        get() = when (val c = category) {
            is RealCategory -> c.category
            else -> throw IllegalStateException("DelayedCategory not a RealCategory: $c")
        }
}
