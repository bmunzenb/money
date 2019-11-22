package com.munzenberger.money.app

import com.munzenberger.money.app.model.*
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.core.*
import io.reactivex.Completable
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import java.time.LocalDate

abstract class EditTransferBase {

    val selectedTypeProperty = SimpleObjectProperty<TransactionType>()
    val selectedCategoryProperty = SimpleObjectProperty<DelayedCategory>()
    val amountProperty = SimpleObjectProperty<Money>()
    val memoProperty = SimpleStringProperty()
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

    private val editTransfers = FXCollections.observableArrayList<EditTransfer>().apply {
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
        this.transaction = Transaction()

        selectedAccountProperty.addListener { _, _, newValue ->

            val selectedType = selectedTypeProperty.value

            when (newValue) {
                null -> types.clear()
                else -> types.setAll(TransactionType.getTypes(newValue.accountType!!))
            }

            selectedTypeProperty.value = when (selectedType) {
                is TransactionType.Credit -> types.find { it is TransactionType.Credit }
                is TransactionType.Debit -> types.find { it is TransactionType.Debit }
                else -> null
            }
        }

        selectedAccountProperty.value = transaction.account

        accounts.subscribeTo(Account.getAssetsAndLiabilities(database))

        dateProperty.value = transaction.date?.toLocalDate() ?: LocalDate.now()

        selectedPayeeProperty.value = transaction.payee

        payees.subscribeTo(Payee.getAllSorted(database))

        categories.subscribeTo(Category.observableGetAll(database).map {
            it.map { c -> RealCategory(c) }
        })

        transaction.getTransfers(database)
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

        val save = database.transaction { tx ->

            val completables = mutableListOf<Completable>()

            transaction.apply {
                account = selectedAccountProperty.value
                date = dateProperty.value.toDate()
                payee = selectedPayeeProperty.value
                memo = memoProperty.value
            }

            completables.add(transaction.observableSave(tx))

            editTransfers.forEachIndexed { index, editTransfer ->
                val transfer = when {
                    // update existing transfer
                    index < transfers.size -> transfers[index]
                    // create new transfer
                    else -> Transfer().apply { setTransaction(transaction) }
                }
                editTransfer.update(transfer)
                completables.add(transfer.observableSave(tx))
            }

            // delete any transfers in excess of the number being updated/created
            completables.addAll(transfers.drop(editTransfers.size).map { it.observableDelete(tx) })

            Completable.concat(completables)
        }

        saveStatus.subscribeTo(save)
    }

    override fun close() {
        // do nothing
    }
}

class EditTransfer(transfer: Transfer? = null) : EditTransferBase() {

    init {
        // TODO: initialize the properties
    }

    fun update(transfer: Transfer) {
        transfer.apply {

        }
    }
}
