package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.core.model.PayeeTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferEntryTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetConsumer
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.getLocalDate
import com.munzenberger.money.sql.getLongOrNull
import com.munzenberger.money.sql.updateQuery
import java.sql.ResultSet
import java.time.LocalDate

sealed class AccountEntry {
    abstract val transactionId: TransactionIdentity
    abstract val date: LocalDate
    abstract val payeeId: PayeeIdentity?
    abstract val payeeName: String?
    abstract val amount: Money
    abstract val balance: Money
    abstract val memo: String?
    abstract val number: String?
    abstract val status: TransactionStatus

    abstract fun updateStatus(
        status: TransactionStatus,
        executor: QueryExecutor,
    )

    data class Transaction(
        override val transactionId: TransactionIdentity,
        override val date: LocalDate,
        override val payeeId: PayeeIdentity?,
        override val payeeName: String?,
        override val amount: Money,
        override val balance: Money,
        override val memo: String?,
        override val number: String?,
        override val status: TransactionStatus,
        val details: List<Detail>,
    ) : AccountEntry() {
        sealed class Detail {
            abstract val orderInTransaction: Int

            data class Transfer(
                val transferId: TransferEntryIdentity,
                val accountId: AccountIdentity,
                val accountName: String,
                override val orderInTransaction: Int,
            ) : Detail()

            data class Category(
                val entryId: CategoryEntryIdentity,
                val categoryId: CategoryIdentity,
                val categoryName: String,
                val parentCategoryName: String?,
                override val orderInTransaction: Int,
            ) : Detail()
        }

        override fun updateStatus(
            status: TransactionStatus,
            executor: QueryExecutor,
        ) {
            val query =
                updateQuery(TransactionTable.tableName) {
                    set(TransactionTable.TRANSACTION_STATUS, status.name)
                    where(TransactionTable.identityColumn.eq(transactionId.value))
                }

            executor.executeUpdate(query)
        }
    }

    data class Transfer(
        val transferId: TransferEntryIdentity,
        override val transactionId: TransactionIdentity,
        override val date: LocalDate,
        override val payeeId: PayeeIdentity?,
        override val payeeName: String?,
        override val amount: Money,
        override val balance: Money,
        override val memo: String?,
        override val number: String?,
        override val status: TransactionStatus,
        val transactionAccountId: AccountIdentity,
        val transactionAccountName: String,
    ) : AccountEntry() {
        override fun updateStatus(
            status: TransactionStatus,
            executor: QueryExecutor,
        ) {
            val query =
                updateQuery(TransferEntryTable.tableName) {
                    set(TransferEntryTable.TRANSFER_ENTRY_STATUS, status.name)
                    where(TransferEntryTable.identityColumn.eq(transferId.value))
                }

            executor.executeUpdate(query)
        }
    }
}

private class AccountEntryCollector {
    sealed class Collector {
        abstract val transactionId: Long
        abstract val date: LocalDate
        abstract val totalAmount: Long

        class Transaction(
            override val transactionId: Long,
            override val date: LocalDate,
            val payeeId: Long?,
            val payeeName: String?,
            var amount: Long = 0,
            val memo: String?,
            val number: String?,
            val status: String,
            val details: MutableList<AccountEntry.Transaction.Detail> = mutableListOf(),
        ) : Collector() {
            override val totalAmount: Long
                get() = amount
        }

        class Transfer(
            val transferId: Long,
            override val transactionId: Long,
            override val date: LocalDate,
            val payeeId: Long?,
            val payeeName: String?,
            override val totalAmount: Long,
            val memo: String?,
            val number: String?,
            val status: String,
            val transactionAccountId: Long,
            val transactionAccountName: String,
        ) : Collector()
    }

    private val transactions = mutableMapOf<Long, Collector.Transaction>()

    private val transfers = mutableListOf<Collector.Transfer>()

    fun collectTransaction(
        transactionId: Long,
        date: LocalDate,
        payeeId: Long?,
        payeeName: String?,
        memo: String?,
        number: String?,
        status: String,
    ) {
        transactions.getOrPut(transactionId) {
            Collector.Transaction(
                transactionId = transactionId,
                date = date,
                payeeId = payeeId,
                payeeName = payeeName,
                memo = memo,
                number = number,
                status = status,
            )
        }
    }

    fun collectTransactionTransferEntry(
        transactionId: Long,
        amount: Long,
        transferId: Long,
        transferAccountId: Long,
        transferAccountName: String,
        transferOrderInTransaction: Int,
    ) {
        val t = transactions[transactionId] ?: error("No transaction with id: $transactionId")

        t.amount += amount

        t.details +=
            AccountEntry.Transaction.Detail.Transfer(
                transferId = TransferEntryIdentity(transferId),
                accountId = AccountIdentity(transferAccountId),
                accountName = transferAccountName,
                orderInTransaction = transferOrderInTransaction,
            )
    }

    fun collectTransactionCategoryEntry(
        transactionId: Long,
        amount: Long,
        entryId: Long,
        entryCategoryId: Long,
        entryCategoryName: String,
        entryParentCategoryName: String?,
        entryOrderInTransaction: Int,
    ) {
        val t = transactions[transactionId] ?: error("No transaction with id: $transactionId")

        t.amount += amount

        t.details +=
            AccountEntry.Transaction.Detail.Category(
                entryId = CategoryEntryIdentity(entryId),
                categoryId = CategoryIdentity(entryCategoryId),
                categoryName = entryCategoryName,
                parentCategoryName = entryParentCategoryName,
                orderInTransaction = entryOrderInTransaction,
            )
    }

    fun collectTransfer(
        transferId: Long,
        transactionId: Long,
        date: LocalDate,
        payeeId: Long?,
        payeeName: String?,
        amount: Long,
        memo: String?,
        number: String?,
        status: String,
        transactionAccountId: Long,
        transactionAccountName: String,
    ) {
        transfers +=
            Collector.Transfer(
                transferId = transferId,
                transactionId = transactionId,
                date = date,
                payeeId = payeeId,
                payeeName = payeeName,
                totalAmount = -amount,
                memo = memo,
                number = number,
                status = status,
                transactionAccountId = transactionAccountId,
                transactionAccountName = transactionAccountName,
            )
    }

    fun getAccountEntries(initialBalance: Money?): List<AccountEntry> {
        var balance = initialBalance?.value ?: 0

        val collectors =
            (transactions.values + transfers).sortedWith(
                compareBy({ it.date }, { it.transactionId }),
            )

        return collectors.map { c ->

            balance += c.totalAmount

            when (c) {
                is Collector.Transaction ->
                    AccountEntry.Transaction(
                        transactionId = TransactionIdentity(c.transactionId),
                        date = c.date,
                        payeeId = c.payeeId?.let { PayeeIdentity(it) },
                        payeeName = c.payeeName,
                        amount = Money.valueOf(c.totalAmount),
                        balance = Money.valueOf(balance),
                        memo = c.memo,
                        number = c.number,
                        status = TransactionStatus.valueOf(c.status),
                        details = c.details.sortedBy { it.orderInTransaction },
                    )

                is Collector.Transfer ->
                    AccountEntry.Transfer(
                        transferId = TransferEntryIdentity(c.transferId),
                        transactionId = TransactionIdentity(c.transactionId),
                        date = c.date,
                        payeeId = c.payeeId?.let { PayeeIdentity(it) },
                        payeeName = c.payeeName,
                        amount = Money.valueOf(c.totalAmount),
                        balance = Money.valueOf(balance),
                        memo = c.memo,
                        number = c.number,
                        status = TransactionStatus.valueOf(c.status),
                        transactionAccountId = AccountIdentity(c.transactionAccountId),
                        transactionAccountName = c.transactionAccountName,
                    )
            }
        }
    }
}

/**
 * Collects all transactions associated with the specified account.  Each transaction is the parent to one or more
 * transfers targeting another account, or a credit/debit for a category.
 */
private class TransactionResultSetHandler(accountId: AccountIdentity, private val collector: AccountEntryCollector) : ResultSetConsumer {
    private val sql =
        """
        SELECT
            ${TransactionTable.identityColumn},
            ${TransactionTable.TRANSACTION_DATE},
            ${PayeeTable.identityColumn},
            ${PayeeTable.PAYEE_NAME},
            ${TransactionTable.TRANSACTION_MEMO},
            ${TransactionTable.TRANSACTION_NUMBER},
            ${TransactionTable.TRANSACTION_STATUS}
        FROM ${TransactionTable.tableName}
        LEFT JOIN ${PayeeTable.tableName} ON ${PayeeTable.tableName}.${PayeeTable.identityColumn} = ${TransactionTable.tableName}.${TransactionTable.TRANSACTION_PAYEE_ID}
        WHERE ${TransactionTable.TRANSACTION_ACCOUNT_ID} = ?
        """.trimIndent()

    val query = Query(sql, listOf(accountId.value))

    override fun accept(rs: ResultSet) {
        while (rs.next()) {
            collector.collectTransaction(
                transactionId = rs.getLong(TransactionTable.identityColumn),
                date = rs.getLocalDate(TransactionTable.TRANSACTION_DATE),
                payeeId = rs.getLongOrNull(PayeeTable.identityColumn),
                payeeName = rs.getString(PayeeTable.PAYEE_NAME),
                memo = rs.getString(TransactionTable.TRANSACTION_MEMO),
                number = rs.getString(TransactionTable.TRANSACTION_NUMBER),
                status = rs.getString(TransactionTable.TRANSACTION_STATUS),
            )
        }
    }
}

/**
 * Collects all transfers where the parent transaction is associated with the specified account.
 */
private class TransactionTransferEntryResultSetHandler(
    accountId: AccountIdentity,
    private val collector: AccountEntryCollector,
) : ResultSetConsumer {
    private val sql =
        """
        SELECT
            ${TransactionTable.identityColumn},
            ${TransferEntryTable.TRANSFER_ENTRY_AMOUNT},
            ${TransferEntryTable.identityColumn},
            ${AccountTable.identityColumn},
            ${AccountTable.ACCOUNT_NAME},
            ${TransferEntryTable.TRANSFER_ENTRY_ORDER_IN_TRANSACTION}
        FROM ${TransactionTable.tableName}
        INNER JOIN ${TransferEntryTable.tableName} ON ${TransferEntryTable.tableName}.${TransferEntryTable.TRANSFER_ENTRY_TRANSACTION_ID} = ${TransactionTable.tableName}.${TransactionTable.identityColumn}
        INNER JOIN ${AccountTable.tableName} ON ${AccountTable.tableName}.${AccountTable.identityColumn} = ${TransferEntryTable.tableName}.${TransferEntryTable.TRANSFER_ENTRY_ACCOUNT_ID}
        WHERE ${TransactionTable.TRANSACTION_ACCOUNT_ID} = ?
        """.trimIndent()

    val query = Query(sql, listOf(accountId.value))

    override fun accept(rs: ResultSet) {
        while (rs.next()) {
            collector.collectTransactionTransferEntry(
                transactionId = rs.getLong(TransactionTable.identityColumn),
                amount = rs.getLong(TransferEntryTable.TRANSFER_ENTRY_AMOUNT),
                transferId = rs.getLong(TransferEntryTable.identityColumn),
                transferAccountId = rs.getLong(AccountTable.identityColumn),
                transferAccountName = rs.getString(AccountTable.ACCOUNT_NAME),
                transferOrderInTransaction = rs.getInt(TransferEntryTable.TRANSFER_ENTRY_ORDER_IN_TRANSACTION),
            )
        }
    }
}

/**
 * Collects all category entries where the parent transaction is associated with the specified account.
 */
private class TransactionCategoryEntryResultSetHandler(
    accountId: AccountIdentity,
    private val collector: AccountEntryCollector,
) : ResultSetConsumer {
    private val sql =
        """
        SELECT
            ${TransactionTable.identityColumn},
            ${CategoryEntryTable.identityColumn},
            ${CategoryEntryTable.CATEGORY_ENTRY_AMOUNT},
            ${CategoryTable.tableName}.${CategoryTable.identityColumn} AS CATEGORY_ID,
            ${CategoryTable.tableName}.${CategoryTable.CATEGORY_NAME} AS CATEGORY_NAME,
            PARENT_CATEGORIES.${CategoryTable.CATEGORY_NAME} AS PARENT_CATEGORY_NAME,
            ${CategoryEntryTable.CATEGORY_ENTRY_ORDER_IN_TRANSACTION}
        FROM ${TransactionTable.tableName}
        INNER JOIN ${CategoryEntryTable.tableName} ON ${CategoryEntryTable.tableName}.${CategoryEntryTable.CATEGORY_ENTRY_TRANSACTION_ID} = ${TransactionTable.tableName}.${TransactionTable.identityColumn}
        INNER JOIN ${CategoryTable.tableName} ON ${CategoryTable.tableName}.${CategoryTable.identityColumn} = ${CategoryEntryTable.tableName}.${CategoryEntryTable.CATEGORY_ENTRY_CATEGORY_ID}
        LEFT JOIN ${CategoryTable.tableName} AS PARENT_CATEGORIES ON ${CategoryTable.tableName}.${CategoryTable.CATEGORY_PARENT_ID} = PARENT_CATEGORIES.${CategoryTable.identityColumn}
        WHERE ${TransactionTable.TRANSACTION_ACCOUNT_ID} = ?
        """.trimIndent()

    val query = Query(sql, listOf(accountId.value))

    override fun accept(rs: ResultSet) {
        while (rs.next()) {
            collector.collectTransactionCategoryEntry(
                transactionId = rs.getLong(TransactionTable.identityColumn),
                amount = rs.getLong(CategoryEntryTable.CATEGORY_ENTRY_AMOUNT),
                entryId = rs.getLong(CategoryEntryTable.identityColumn),
                entryCategoryId = rs.getLong("CATEGORY_ID"),
                entryCategoryName = rs.getString("CATEGORY_NAME"),
                entryParentCategoryName = rs.getString("PARENT_CATEGORY_NAME"),
                entryOrderInTransaction = rs.getInt(CategoryEntryTable.CATEGORY_ENTRY_ORDER_IN_TRANSACTION),
            )
        }
    }
}

/**
 * Collects all transfers that target the specified account.
 */
private class TransferEntryResultSetHandler(accountId: AccountIdentity, private val collector: AccountEntryCollector) : ResultSetConsumer {
    private val sql =
        """
        SELECT
            ${TransferEntryTable.identityColumn},
            ${TransactionTable.identityColumn},
            ${TransactionTable.TRANSACTION_DATE},
            ${PayeeTable.identityColumn},
            ${PayeeTable.PAYEE_NAME},
            ${TransferEntryTable.TRANSFER_ENTRY_AMOUNT},
            ${TransferEntryTable.TRANSFER_ENTRY_MEMO},
            ${TransferEntryTable.TRANSFER_ENTRY_NUMBER},
            ${TransferEntryTable.TRANSFER_ENTRY_STATUS},
            ${AccountTable.identityColumn},
            ${AccountTable.ACCOUNT_NAME}
        FROM ${TransferEntryTable.tableName}
        INNER JOIN ${TransactionTable.tableName} ON ${TransactionTable.tableName}.${TransactionTable.identityColumn} = ${TransferEntryTable.tableName}.${TransferEntryTable.TRANSFER_ENTRY_TRANSACTION_ID}
        LEFT JOIN ${PayeeTable.tableName} ON ${PayeeTable.tableName}.${PayeeTable.identityColumn} = ${TransactionTable.tableName}.${TransactionTable.TRANSACTION_PAYEE_ID}
        INNER JOIN ${AccountTable.tableName} ON ${TransactionTable.tableName}.${TransactionTable.TRANSACTION_ACCOUNT_ID} = ${AccountTable.tableName}.${AccountTable.identityColumn}
        WHERE ${TransferEntryTable.TRANSFER_ENTRY_ACCOUNT_ID} = ?
        """.trimIndent()

    val query = Query(sql, listOf(accountId.value))

    override fun accept(rs: ResultSet) {
        while (rs.next()) {
            collector.collectTransfer(
                transferId = rs.getLong(TransferEntryTable.identityColumn),
                transactionId = rs.getLong(TransactionTable.identityColumn),
                date = rs.getLocalDate(TransactionTable.TRANSACTION_DATE),
                payeeId = rs.getLongOrNull(PayeeTable.identityColumn),
                payeeName = rs.getString(PayeeTable.PAYEE_NAME),
                amount = rs.getLong(TransferEntryTable.TRANSFER_ENTRY_AMOUNT),
                memo = rs.getString(TransferEntryTable.TRANSFER_ENTRY_MEMO),
                number = rs.getString(TransferEntryTable.TRANSFER_ENTRY_NUMBER),
                status = rs.getString(TransferEntryTable.TRANSFER_ENTRY_STATUS),
                transactionAccountId = rs.getLong(AccountTable.identityColumn),
                transactionAccountName = rs.getString(AccountTable.ACCOUNT_NAME),
            )
        }
    }
}

fun Account.getAccountEntries(executor: QueryExecutor): List<AccountEntry> {
    val accountId = identity ?: error("Can't get entries for an unsaved account.")

    val collector = AccountEntryCollector()

    // 1: Get all direct transactions associated with this account
    TransactionResultSetHandler(accountId, collector).apply {
        executor.executeQuery(query, this)
    }

    // 2: Get all transfer entries associated with transactions for this account
    TransactionTransferEntryResultSetHandler(accountId, collector).apply {
        executor.executeQuery(query, this)
    }

    // 3: Get all category entries associated with transactions for this account
    TransactionCategoryEntryResultSetHandler(accountId, collector).apply {
        executor.executeQuery(query, this)
    }

    // 4: Get all transfer entries associated with this account
    TransferEntryResultSetHandler(accountId, collector).apply {
        executor.executeQuery(query, this)
    }

    return collector.getAccountEntries(initialBalance)
}
