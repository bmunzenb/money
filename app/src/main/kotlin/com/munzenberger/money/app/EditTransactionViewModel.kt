package com.munzenberger.money.app

import com.munzenberger.money.app.model.DelayedCategory
import com.munzenberger.money.app.model.PendingCategory
import com.munzenberger.money.app.model.RealCategory
import com.munzenberger.money.app.model.getAssetsAndLiabilities
import com.munzenberger.money.app.model.observableGetTransfers
import com.munzenberger.money.app.model.toDate
import com.munzenberger.money.app.model.toLocalDate
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

    val selectedTypeProperty = SimpleObjectProperty<TransactionType>()
    val selectedCategoryProperty = SimpleObjectProperty<DelayedCategory>()
    val amountProperty = SimpleObjectProperty<Money>()
    val memoProperty = SimpleStringProperty()

    var transactionType: TransactionType?
        get() = selectedTypeProperty.value
        set(value) { selectedTypeProperty.value = value }

    var category: DelayedCategory?
        get() = selectedCategoryProperty.value
        set(value) { selectedCategoryProperty.value = value }
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

    val editTransfers: ObservableList<EditTransfer> = FXCollections.observableArrayList<EditTransfer>().apply {
        addListener(ListChangeListener {

            it.list.firstOrNull()?.let { first ->
                selectedTypeProperty.unbindBidirectional(first.selectedTypeProperty)
                selectedCategoryProperty.unbindBidirectional(first.selectedCategoryProperty)
                amountProperty.unbindBidirectional(first.amountProperty)
            }

            when (it.list.size){
                1 -> {
                    it.list.first().let { first ->
                        selectedTypeProperty.bindBidirectional(first.selectedTypeProperty)
                        selectedCategoryProperty.bindBidirectional(first.selectedCategoryProperty)
                        amountProperty.bindBidirectional(first.amountProperty)
                    }
                    typeDisabled.value = false
                    categoryDisabled.value = false
                    amountDisabled.value = false
                }
                else -> {
                    typeDisabled.value = true
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
                .or(selectedCategoryProperty.isNull)
                .or(amountProperty.isNull))
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

            transactionType = types.find { it.javaClass == selectedType?.javaClass }
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
            transfers.isEmpty() -> listOf(Transfer().apply {
                setTransaction(transaction)
            })
            else -> transfers
        }

        editTransfers.setAll(this.transfers.map { EditTransfer(it) })

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
                    is PendingCategory -> edit.category = c.toRealCategory(tx, edit.transactionType!!)
                }

                val transfer = when {
                    // update existing transfer
                    index < transfers.size -> transfers[index]
                    // create new transfer
                    else -> Transfer().apply { setTransaction(transaction) }
                }

                transfer.apply {
                    this.amount = edit.amountValue
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

    override fun close() {
        // do nothing
    }
}

class EditTransfer(transfer: Transfer? = null) : EditTransferBase() {

    init {

        this.category = when (val c = transfer?.category) {
            null -> null
            else -> RealCategory(c)
        }

        // TODO: determine the transaction type

        amountProperty.value = transfer?.amount?.let { Money.valueOf(it) }

        memoProperty.value = transfer?.memo
    }

    val amountValue: Long
        get() = when (transactionType) {
            is TransactionType.Credit -> amountProperty.value.value
            is TransactionType.Debit -> -amountProperty.value.value
            else -> throw IllegalStateException("TransactionType not set")
        }

    val realCategory: Category
        get() = when (val c = category) {
            is RealCategory -> c.category
            else -> throw IllegalStateException("DelayedCategory not a RealCategory: $c")
        }

    val memo: String?
        get() = memoProperty.value
}
