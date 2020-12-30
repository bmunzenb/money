package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.getLocalDate
import com.munzenberger.money.sql.getLongOrNull
import java.lang.IllegalStateException
import java.sql.ResultSet
import java.time.LocalDate

sealed class AccountEntry {

    abstract val transactionId: Long
    abstract val date: LocalDate
    abstract val payeeId: Long?
    abstract val payeeName: String?
    abstract val amount: Money
    abstract val balance: Money
    abstract val memo: String?
    abstract val number: String?
    abstract val status: TransactionStatus

    abstract fun updateStatus(status: TransactionStatus, executor: QueryExecutor)

    data class Transaction(
            override val transactionId: Long,
            override val date: LocalDate,
            override val payeeId: Long?,
            override val payeeName: String?,
            override val amount: Money,
            override val balance: Money,
            override val memo: String?,
            override val number: String?,
            override val status: TransactionStatus,
            val details: List<Detail>
    ) : AccountEntry() {

        sealed class Detail {

            abstract val orderInTransaction: Int

            data class Transfer(
                    val transferId: Long,
                    val accountId: Long,
                    val accountName: String,
                    override val orderInTransaction: Int
            ) : Detail()

            data class Entry(
                    val entryId: Long,
                    val categoryId: Long,
                    val categoryName: String,
                    val parentCategoryName: String?,
                    override val orderInTransaction: Int
            ) : Detail()
        }

        override fun updateStatus(status: TransactionStatus, executor: QueryExecutor) {

            val query = Query.update(TransactionTable.name)
                    .set(TransactionTable.statusColumn, status.name)
                    .where(TransactionTable.identityColumn.eq(transactionId))
                    .build()

            executor.executeUpdate(query)
        }
    }

    data class Transfer(
            val transferId: Long,
            override val transactionId: Long,
            override val date: LocalDate,
            override val payeeId: Long?,
            override val payeeName: String?,
            override val amount: Money,
            override val balance: Money,
            override val memo: String?,
            override val number: String?,
            override val status: TransactionStatus,
            val transactionAccountId: Long,
            val transactionAccountName: String
    ) : AccountEntry() {

        override fun updateStatus(status: TransactionStatus, executor: QueryExecutor) {

            val query = Query.update(TransferTable.name)
                    .set(TransferTable.statusColumn, status.name)
                    .where(TransferTable.identityColumn.eq(transferId))
                    .build()

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
                val details: MutableList<AccountEntry.Transaction.Detail> = mutableListOf()
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
                val transactionAccountName: String
        ) : Collector()
    }

    private val transactions = mutableMapOf<Long, Collector.Transaction>()

    private val transfers = mutableListOf<Collector.Transfer>()

    fun collectTransactionTransfer(
            transactionId: Long,
            date: LocalDate,
            payeeId: Long?,
            payeeName: String?,
            amount: Long,
            memo: String?,
            number: String?,
            status: String,
            transferId: Long,
            transferAccountId: Long,
            transferAccountName: String,
            transferOrderInTransaction: Int
    ) {
        val t = transactions.getOrPut(transactionId) {
            Collector.Transaction(
                    transactionId = transactionId,
                    date = date,
                    payeeId = payeeId,
                    payeeName = payeeName,
                    memo = memo,
                    number = number,
                    status = status
            )
        }

        t.amount += amount

        t.details += AccountEntry.Transaction.Detail.Transfer(
                transferId = transferId,
                accountId = transferAccountId,
                accountName = transferAccountName,
                orderInTransaction = transferOrderInTransaction
        )
    }

    fun collectTransactionEntry(
            transactionId: Long,
            date: LocalDate,
            payeeId: Long?,
            payeeName: String?,
            amount: Long,
            memo: String?,
            number: String?,
            status: String,
            entryId: Long,
            entryCategoryId: Long,
            entryCategoryName: String,
            entryParentCategoryName: String?,
            entryOrderInTransaction: Int
    ) {
        val t = transactions.getOrPut(transactionId) {
            Collector.Transaction(
                    transactionId = transactionId,
                    date = date,
                    payeeId = payeeId,
                    payeeName = payeeName,
                    memo = memo,
                    number = number,
                    status = status
            )
        }

        t.amount += amount

        t.details += AccountEntry.Transaction.Detail.Entry(
                entryId = entryId,
                categoryId = entryCategoryId,
                categoryName = entryCategoryName,
                parentCategoryName = entryParentCategoryName,
                orderInTransaction = entryOrderInTransaction
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
            transactionAccountName: String
    ) {
        transfers += Collector.Transfer(
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
                transactionAccountName = transactionAccountName
        )
    }

    fun getAccountEntries(initialBalance: Money?): List<AccountEntry> {

        var balance = initialBalance?.value ?: 0

        val collectors = (transactions.values + transfers).sortedWith(
                compareBy({ it.date }, { it.transactionId })
        )

        return collectors.map { c ->

            balance += c.totalAmount

            when (c) {
                is Collector.Transaction -> AccountEntry.Transaction(
                        transactionId = c.transactionId,
                        date = c.date,
                        payeeId = c.payeeId,
                        payeeName = c.payeeName,
                        amount = Money.valueOf(c.totalAmount),
                        balance = Money.valueOf(balance),
                        memo = c.memo,
                        number = c.number,
                        status = TransactionStatus.valueOf(c.status),
                        details = c.details.sortedBy { it.orderInTransaction }
                )

                is Collector.Transfer -> AccountEntry.Transfer(
                        transferId = c.transferId,
                        transactionId = c.transactionId,
                        date = c.date,
                        payeeId = c.payeeId,
                        payeeName = c.payeeName,
                        amount = Money.valueOf(c.totalAmount),
                        balance = Money.valueOf(balance),
                        memo = c.memo,
                        number = c.number,
                        status = TransactionStatus.valueOf(c.status),
                        transactionAccountId = c.transactionAccountId,
                        transactionAccountName = c.transactionAccountName
                )
            }
        }
    }
}

private class TransactionTransferResultSetHandler(accountId: Long, private val collector: AccountEntryCollector) : ResultSetHandler {

    private val sql = """
        SELECT
            TRANSACTION_ID,
            TRANSACTION_DATE,
            PAYEE_ID,
            PAYEE_NAME,
            TRANSFER_AMOUNT,
            TRANSACTION_MEMO,
            TRANSACTION_NUMBER,
            TRANSACTION_STATUS,
            TRANSFER_ID,
            ACCOUNT_ID,
            ACCOUNT_NAME,
            TRANSFER_ORDER_IN_TRANSACTION
        FROM TRANSACTIONS
        INNER JOIN TRANSFERS ON TRANSFERS.TRANSFER_TRANSACTION_ID = TRANSACTIONS.TRANSACTION_ID
        LEFT JOIN PAYEES ON PAYEES.PAYEE_ID = TRANSACTIONS.TRANSACTION_PAYEE_ID
        INNER JOIN ACCOUNTS ON ACCOUNTS.ACCOUNT_ID = TRANSFERS.TRANSFER_ACCOUNT_ID
        WHERE TRANSACTION_ACCOUNT_ID = ?
    """.trimIndent()

    val query = Query(sql, listOf(accountId))

    override fun accept(rs: ResultSet) {
        while (rs.next()) {
            collector.collectTransactionTransfer(
                    transactionId = rs.getLong("TRANSACTION_ID"),
                    date = rs.getLocalDate("TRANSACTION_DATE"),
                    payeeId = rs.getLongOrNull("PAYEE_ID"),
                    payeeName = rs.getString("PAYEE_NAME"),
                    amount = rs.getLong("TRANSFER_AMOUNT"),
                    memo = rs.getString("TRANSACTION_MEMO"),
                    number = rs.getString("TRANSACTION_NUMBER"),
                    status = rs.getString("TRANSACTION_STATUS"),
                    transferId = rs.getLong("TRANSFER_ID"),
                    transferAccountId = rs.getLong("ACCOUNT_ID"),
                    transferAccountName = rs.getString("ACCOUNT_NAME"),
                    transferOrderInTransaction = rs.getInt("TRANSFER_ORDER_IN_TRANSACTION")
            )
        }
    }
}

private class TransactionEntryResultSetHandler(accountId: Long, private val collector: AccountEntryCollector) : ResultSetHandler {

    private val sql = """
        SELECT
            TRANSACTION_ID,
            TRANSACTION_DATE,
            PAYEE_ID,
            PAYEE_NAME,
            ENTRY_ID,
            ENTRY_AMOUNT,
            TRANSACTION_MEMO,
            TRANSACTION_NUMBER,
            TRANSACTION_STATUS,
            CATEGORIES.CATEGORY_ID AS CATEGORY_ID,
            CATEGORIES.CATEGORY_NAME AS CATEGORY_NAME,
            PARENT_CATEGORIES.CATEGORY_NAME AS PARENT_CATEGORY_NAME,
            ENTRY_ORDER_IN_TRANSACTION
        FROM TRANSACTIONS
        INNER JOIN ENTRIES ON ENTRIES.ENTRY_TRANSACTION_ID = TRANSACTIONS.TRANSACTION_ID
        LEFT JOIN PAYEES ON PAYEES.PAYEE_ID = TRANSACTIONS.TRANSACTION_PAYEE_ID
        INNER JOIN CATEGORIES ON CATEGORIES.CATEGORY_ID = ENTRIES.ENTRY_CATEGORY_ID
        LEFT JOIN CATEGORIES AS PARENT_CATEGORIES ON CATEGORIES.CATEGORY_PARENT_ID = PARENT_CATEGORIES.CATEGORY_ID 
        WHERE TRANSACTION_ACCOUNT_ID = ?
    """.trimIndent()

    val query = Query(sql, listOf(accountId))

    override fun accept(rs: ResultSet) {
        while (rs.next()) {
            collector.collectTransactionEntry(
                    transactionId = rs.getLong("TRANSACTION_ID"),
                    date = rs.getLocalDate("TRANSACTION_DATE"),
                    payeeId = rs.getLongOrNull("PAYEE_ID"),
                    payeeName = rs.getString("PAYEE_NAME"),
                    amount = rs.getLong("ENTRY_AMOUNT"),
                    memo = rs.getString("TRANSACTION_MEMO"),
                    number = rs.getString("TRANSACTION_NUMBER"),
                    status = rs.getString("TRANSACTION_STATUS"),
                    entryId = rs.getLong("ENTRY_ID"),
                    entryCategoryId = rs.getLong("CATEGORY_ID"),
                    entryCategoryName = rs.getString("CATEGORY_NAME"),
                    entryParentCategoryName = rs.getString("PARENT_CATEGORY_NAME"),
                    entryOrderInTransaction = rs.getInt("ENTRY_ORDER_IN_TRANSACTION")
            )
        }
    }
}

private class TransferResultSetHandler(accountId: Long, private val collector: AccountEntryCollector) : ResultSetHandler {

    private val sql = """
        SELECT
            TRANSFER_ID,
            TRANSACTION_ID,
            TRANSACTION_DATE,
            PAYEE_ID,
            PAYEE_NAME,
            TRANSFER_AMOUNT,
            TRANSFER_MEMO,
            TRANSFER_NUMBER,
            TRANSFER_STATUS,
            ACCOUNT_ID,
            ACCOUNT_NAME
        FROM TRANSFERS
        INNER JOIN TRANSACTIONS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
        LEFT JOIN PAYEES ON PAYEES.PAYEE_ID = TRANSACTIONS.TRANSACTION_PAYEE_ID
        INNER JOIN ACCOUNTS ON TRANSACTIONS.TRANSACTION_ACCOUNT_ID = ACCOUNTS.ACCOUNT_ID
        WHERE TRANSFER_ACCOUNT_ID = ?
    """.trimIndent()

    val query = Query(sql, listOf(accountId))

    override fun accept(rs: ResultSet) {
        while (rs.next()) {
            collector.collectTransfer(
                    transferId = rs.getLong("TRANSFER_ID"),
                    transactionId = rs.getLong("TRANSACTION_ID"),
                    date = rs.getLocalDate("TRANSACTION_DATE"),
                    payeeId = rs.getLongOrNull("PAYEE_ID"),
                    payeeName = rs.getString("PAYEE_NAME"),
                    amount = rs.getLong("TRANSFER_AMOUNT"),
                    memo = rs.getString("TRANSFER_MEMO"),
                    number = rs.getString("TRANSFER_NUMBER"),
                    status = rs.getString("TRANSFER_STATUS"),
                    transactionAccountId = rs.getLong("ACCOUNT_ID"),
                    transactionAccountName = rs.getString("ACCOUNT_NAME")
            )
        }
    }
}


fun Account.getAccountEntries(executor: QueryExecutor): List<AccountEntry> {

    val accountId = identity ?: throw IllegalStateException("Can't get entries for an unsaved account.")

    val collector = AccountEntryCollector()

    TransactionTransferResultSetHandler(accountId, collector).apply {
        executor.executeQuery(query, this)
    }

    TransactionEntryResultSetHandler(accountId, collector).apply {
        executor.executeQuery(query, this)
    }

    TransferResultSetHandler(accountId, collector).apply {
        executor.executeQuery(query, this)
    }

    return collector.getAccountEntries(initialBalance)
}
