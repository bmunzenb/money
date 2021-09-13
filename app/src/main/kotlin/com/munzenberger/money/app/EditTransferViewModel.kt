package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.executeAsync
import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.model.displayName
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.app.property.asyncValue
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.Transfer
import com.munzenberger.money.core.TransferResultSetMapper
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import io.reactivex.rxjava3.core.Single
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import java.sql.ResultSet
import java.time.LocalDate

class EditTransferViewModel {

    private data class TransferResult(
            val transfer: Transfer,
            val transaction: Transaction
    )

    private val types = SimpleListProperty<TransactionType>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val saveStatus = SimpleAsyncStatusProperty()
    private val transactionStatus = SimpleStringProperty()
    private val notValid = SimpleBooleanProperty()
    private val disabled = SimpleBooleanProperty(true)

    val typesProperty: ReadOnlyListProperty<TransactionType> = types
    val selectedTypeProperty = SimpleObjectProperty<TransactionType>()
    val dateProperty = SimpleObjectProperty<LocalDate>()
    val numberProperty = SimpleStringProperty()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee?>()
    val amountProperty = SimpleObjectProperty<Money>()
    val memoProperty = SimpleStringProperty()
    val saveStatusProperty: ReadOnlyAsyncStatusProperty = saveStatus
    val transactionStatusProperty: ReadOnlyStringProperty = transactionStatus
    val notValidProperty: BooleanProperty = notValid
    val disabledProperty: BooleanProperty = disabled

    private lateinit var database: MoneyDatabase
    private lateinit var transfer: Transfer
    private lateinit var transaction: Transaction

    init {
        notValid.bind(disabledProperty
                .or(selectedTypeProperty.isNull)
                .or(dateProperty.isNull)
                .or(amountProperty.isNull))
    }

    fun start(database: MoneyDatabase, transferId: Long) {

        this.database = database

        payees.setValueAsync { Payee.getAll(database).sortedBy { it.name } }

        Single.fromCallable { getTransferResult(database, transferId) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe({ onTransferResult(it.transfer, it.transaction) }, ::onError)
    }

    private fun getTransferResult(database: MoneyDatabase, transferId: Long): TransferResult {

        var transactionId: Long? = null

        val transferMapper = object : ResultSetMapper<Transfer> {
            override fun apply(rs: ResultSet): Transfer {
                transactionId = rs.getLong(TransferTable.transactionColumn)
                return TransferResultSetMapper().apply(rs)
            }
        }

        val transfer = TransferTable
                .select(transferId)
                .build()
                .let { database.getFirst(it, transferMapper) }
                ?: throw PersistableNotFoundException(Transfer::class, transferId)

        val transaction = Transaction.get(transactionId!!, database)
                ?: throw PersistableNotFoundException(Transaction::class, transactionId!!)

        return TransferResult(transfer = transfer, transaction = transaction)
    }

    private fun onTransferResult(transfer: Transfer, transaction: Transaction) {

        this.transfer = transfer
        this.transaction = transaction

        val typesList = TransactionType.getTypes(transfer.account!!.accountType!!)

        types.value = FXCollections.observableArrayList(typesList)

        // transfers follow the opposite rules from transactions:
        // a negative amount is a credit to the transfer account
        val selectedTypeVariant = when (transfer.amount?.isNegative) {
            true -> TransactionType.Variant.CREDIT
            else -> TransactionType.Variant.DEBIT
        }

        selectedTypeProperty.value = typesList.find { it.variant == selectedTypeVariant }

        dateProperty.value = transaction.date

        numberProperty.value = transfer.number

        selectedPayeeProperty.value = transaction.payee

        amountProperty.value = transfer.amount?.forTransferType(selectedTypeVariant)

        memoProperty.value = transfer.memo

        transactionStatus.value = transfer.status?.displayName

        disabled.value = false
    }

    private fun onError(error: Throwable) {
        // TODO move to controller
        // idea: create a "disabled" async object property that can be bound by the controller
        ErrorAlert.showAndWait(error)
    }

    fun save() {

        saveStatus.executeAsync {

            database.transaction { tx ->

                transaction.apply {
                    this.date = dateProperty.value
                    this.payee = selectedPayeeProperty.value
                    save(tx)
                }

                transfer.apply {
                    this.number = numberProperty.value
                    this.amount = amountProperty.value.forTransferType(selectedTypeProperty.value)
                    this.memo = memoProperty.value
                    save(tx)
                }
            }
        }
    }
}
