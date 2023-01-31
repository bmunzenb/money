package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountEntry
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferEntryTable
import com.munzenberger.money.sql.DeleteQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
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

    val transactionId: Long
        get() = accountEntry.transactionId

    protected val status = SimpleObjectProperty(accountEntry.status)

    val dateProperty: ReadOnlyObjectProperty<LocalDate> = SimpleObjectProperty(accountEntry.date)
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(accountEntry.number)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountEntry.balance)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(accountEntry.payeeName)
    val statusProperty: ReadOnlyObjectProperty<TransactionStatus> = status
    val amountProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountEntry.amount)

    val debitProperty: ReadOnlyObjectProperty<Money>
    val creditProperty: ReadOnlyObjectProperty<Money>

    abstract val categoryProperty: ReadOnlyStringProperty

    init {
        when {
            accountEntry.amount.isNegative -> {
                debitProperty = SimpleObjectProperty(accountEntry.amount.negate())
                creditProperty = SimpleObjectProperty()
            }
            else -> {
                debitProperty = SimpleObjectProperty()
                creditProperty = SimpleObjectProperty(accountEntry.amount)
            }
        }
    }

    fun updateStatus(status: TransactionStatus, executor: QueryExecutor) {
        accountEntry.updateStatus(status, executor)
        this.status.value = status
    }

    abstract fun delete(executor: QueryExecutor)
}

class FXTransactionAccountEntry(private val transactionEntry: AccountEntry.Transaction) : FXAccountEntry(transactionEntry) {

    override val categoryProperty: ReadOnlyStringProperty

    init {
        val category = when (transactionEntry.details.size) {
            0 -> null
            1 -> transactionEntry.details[0].name
            else -> SPLIT_CATEGORY_NAME
        }

        categoryProperty = SimpleStringProperty(category)
    }

    override fun delete(executor: QueryExecutor) {

        executor.transaction { tx ->

            DeleteQueryBuilder(TransferEntryTable.tableName)
                    .where(TransferEntryTable.transactionColumn.eq(transactionId))
                    .build()
                    .let { tx.executeUpdate(it) }

            DeleteQueryBuilder(CategoryEntryTable.tableName)
                    .where(CategoryEntryTable.transactionColumn.eq(transactionId))
                    .build()
                    .let { tx.executeUpdate(it) }

            DeleteQueryBuilder(TransactionTable.tableName)
                    .where(TransactionTable.identityColumn.eq(transactionId))
                    .build()
                    .let { tx.executeUpdate(it) }
        }
    }
}

class FXTransferAccountEntry(private val transferEntry: AccountEntry.Transfer) : FXAccountEntry(transferEntry) {

    val transferId: Long
        get() = transferEntry.transferId

    override val categoryProperty: ReadOnlyStringProperty =
            SimpleStringProperty("Transfer $CATEGORY_DELIMITER ${transferEntry.transactionAccountName}")

    override fun delete(executor: QueryExecutor) {

        DeleteQueryBuilder(TransferEntryTable.tableName)
                .where(TransferEntryTable.identityColumn.eq(transferId))
                .build()
                .let { executor.executeUpdate(it) }
    }
}

private val AccountEntry.Transaction.Detail.name: String
    get() = when (this) {

        is AccountEntry.Transaction.Detail.Transfer ->
            "Transfer $CATEGORY_DELIMITER $accountName"

        is AccountEntry.Transaction.Detail.Category ->
            when (val p = parentCategoryName) {
                null -> categoryName
                else -> "$p $CATEGORY_DELIMITER $categoryName"
            }
    }
