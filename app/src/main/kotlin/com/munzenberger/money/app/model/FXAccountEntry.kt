package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountEntry
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.TransactionIdentity
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.TransferEntryIdentity
import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferEntryTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.deleteQuery
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.transaction
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.time.LocalDate

sealed class FXAccountEntry(private val accountEntry: AccountEntry) {
    companion object {
        fun of(accountEntry: AccountEntry): FXAccountEntry {
            return when (accountEntry) {
                is AccountEntry.Transaction -> FXTransactionAccountEntry(accountEntry)
                is AccountEntry.Transfer -> FXTransferAccountEntry(accountEntry)
            }
        }
    }

    val transactionId: TransactionIdentity
        get() = accountEntry.transactionId

    protected val status = SimpleObjectProperty(accountEntry.status)

    val dateProperty: ReadOnlyObjectProperty<LocalDate> = SimpleObjectProperty(accountEntry.date)
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(accountEntry.number)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountEntry.balance)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(accountEntry.payeeName)
    val statusProperty: ReadOnlyObjectProperty<TransactionStatus> = status
    val amountProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountEntry.amount)

    abstract val categoryProperty: ReadOnlyStringProperty

    fun updateStatus(
        status: TransactionStatus,
        executor: QueryExecutor,
    ) {
        accountEntry.updateStatus(status, executor)
        this.status.value = status
    }

    abstract fun delete(executor: QueryExecutor)
}

class FXTransactionAccountEntry(private val transactionEntry: AccountEntry.Transaction) : FXAccountEntry(transactionEntry) {
    override val categoryProperty: ReadOnlyStringProperty

    init {
        val category =
            when (transactionEntry.details.size) {
                0 -> null
                1 -> transactionEntry.details[0].name
                else -> SPLIT_CATEGORY_NAME
            }

        categoryProperty = SimpleStringProperty(category)
    }

    override fun delete(executor: QueryExecutor) {
        executor.transaction { tx ->

            deleteQuery(TransferEntryTable.tableName) {
                where(TransferEntryTable.TRANSFER_ENTRY_TRANSACTION_ID.eq(transactionId.value))
            }.let { tx.executeUpdate(it) }

            deleteQuery(CategoryEntryTable.tableName) {
                where(CategoryEntryTable.CATEGORY_ENTRY_TRANSACTION_ID.eq(transactionId.value))
            }.let { tx.executeUpdate(it) }

            deleteQuery(TransactionTable.tableName) {
                where(TransactionTable.identityColumn.eq(transactionId.value))
            }.let { tx.executeUpdate(it) }
        }
    }
}

class FXTransferAccountEntry(private val transferEntry: AccountEntry.Transfer) : FXAccountEntry(transferEntry) {
    val transferId: TransferEntryIdentity
        get() = transferEntry.transferId

    override val categoryProperty: ReadOnlyStringProperty =
        SimpleStringProperty("Transfer $CATEGORY_DELIMITER ${transferEntry.transactionAccountName}")

    override fun delete(executor: QueryExecutor) {
        deleteQuery(TransferEntryTable.tableName) {
            where(TransferEntryTable.identityColumn.eq(transferId.value))
        }.let { executor.executeUpdate(it) }
    }
}

private val AccountEntry.Transaction.Detail.name: String
    get() =
        when (this) {

            is AccountEntry.Transaction.Detail.Transfer ->
                "Transfer $CATEGORY_DELIMITER $accountName"

            is AccountEntry.Transaction.Detail.Category ->
                when (val p = parentCategoryName) {
                    null -> categoryName
                    else -> "$p $CATEGORY_DELIMITER $categoryName"
                }
        }
