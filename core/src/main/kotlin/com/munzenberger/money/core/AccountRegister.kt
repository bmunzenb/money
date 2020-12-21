package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.getIntOrNull
import com.munzenberger.money.sql.getLocalDate
import com.munzenberger.money.sql.getLongOrNull
import com.munzenberger.money.sql.inGroup
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet
import java.time.LocalDate

data class RegisterEntry(
        val transactionId: Long,
        val date: LocalDate,
        val payeeId: Long?,
        val payeeName: String?,
        val amount: Money,
        val balance: Money,
        val memo: String?,
        val number: String?,
        val status: TransactionStatus,
        val details: List<Detail>,
        private val registerAccountId: Long,
        private val transactionAccountId: Long
) {
    sealed class Detail {

        abstract val orderInTransaction: Int

        data class Transfer(
                val transferId: Long,
                val accountId: Long,
                val accountName: String,
                override val orderInTransaction: Int,
                internal val isTransactionAccount: Boolean
        ) : Detail()

        data class Entry(
                val categoryId: Long,
                val categoryName: String,
                val parentCategoryName: String?,
                override val orderInTransaction: Int
        ) : Detail()
    }

    fun updateStatus(status: TransactionStatus, executor: QueryExecutor) = executor.transaction { tx ->

        if (registerAccountId == transactionAccountId) {
            // update the status of the parent transaction
            val query = Query.update(TransactionTable.name)
                    .set(TransactionTable.statusColumn, status.name)
                    .where(TransactionTable.identityColumn.eq(transactionId))
                    .build()

            tx.executeUpdate(query)
        }

        val ts = details.filterIsInstance<Detail.Transfer>()
                .filter { it.isTransactionAccount }
                .map { it.transferId }

        if (ts.isNotEmpty()) {
            // update the status of the child transfers
            val query = Query.update(TransferTable.name)
                    .set(TransferTable.statusColumn, status.name)
                    .where(TransferTable.identityColumn.inGroup(ts))
                    .build()

            tx.executeUpdate(query)
        }
    }
}

private class RegisterCollector(val accountId: Long) {

    private data class TransactionEntry(
            val transactionId: Long,
            val accountId: Long,
            val date: LocalDate,
            val payeeId: Long?,
            val payeeName: String?,
            var amount: Long = 0,
            var memo: String? = null,
            var number: String? = null,
            var status: String? = null,
            var details: List<RegisterEntry.Detail> = emptyList()
    )

    private val entries = mutableMapOf<Long, TransactionEntry>()

    fun collect(
            transactionId: Long,
            transactionAccountId: Long,
            transactionAccountName: String,
            transactionDate: LocalDate,
            transactionNumber: String?,
            transactionMemo: String?,
            transactionStatus: String,
            payeeId: Long?,
            payeeName: String?,
            transferId: Long?,
            transferAccountId: Long?,
            transferAccountName: String?,
            transferAmount: Long?,
            transferNumber: String?,
            transferMemo: String?,
            transferStatus: String?,
            transferOrderInTransaction: Int?,
            entryId: Long?,
            entryAmount: Long?,
            entryCategoryId: Long?,
            entryCategoryName: String?,
            entryParentCategoryName: String?,
            entryOrderInTransaction: Int?
    ) {

        val entry = entries.getOrPut(transactionId) {
            TransactionEntry(
                    transactionId = transactionId,
                    accountId = transactionAccountId,
                    date = transactionDate,
                    payeeId = payeeId,
                    payeeName = payeeName
            )
        }

        if (accountId == transferAccountId) {
            entry.amount -= transferAmount!!
            entry.status = transferStatus
            entry.number = transferNumber
            entry.memo = transferMemo

            // detail is a transfer to the account on the transaction
            entry.details += RegisterEntry.Detail.Transfer(
                    transferId = transferId!!,
                    accountId = transactionAccountId,
                    accountName = transactionAccountName,
                    orderInTransaction = transferOrderInTransaction!!,
                    isTransactionAccount = true
            )
        }

        if (accountId == transactionAccountId) {
            entry.status = transactionStatus
            entry.number = transactionNumber
            entry.memo = transactionMemo

            transferId?.let {
                entry.amount += transferAmount!!

                // detail is a transfer from the account in the transfer record
                entry.details += RegisterEntry.Detail.Transfer(
                        transferId = it,
                        accountId = transferAccountId!!,
                        accountName = transferAccountName!!,
                        orderInTransaction = transferOrderInTransaction!!,
                        isTransactionAccount = false
                )
            }

            entryId?.let {
                entry.amount += entryAmount!!

                // detail is an entry from a category
                entry.details += RegisterEntry.Detail.Entry(
                        categoryId = entryCategoryId!!,
                        categoryName = entryCategoryName!!,
                        parentCategoryName = entryParentCategoryName,
                        orderInTransaction = entryOrderInTransaction!!
                )
            }
        }
    }

    fun getRegisterEntries(initialBalance: Money): List<RegisterEntry> {

        var balance = initialBalance.value

        return entries.values
                .sortedBy { it.date }
                .map { e ->
                    balance += e.amount
                    RegisterEntry(
                            transactionId = e.transactionId,
                            date = e.date,
                            payeeId = e.payeeId,
                            payeeName = e.payeeName,
                            details = e.details.sortedBy { it.orderInTransaction },
                            amount = Money.valueOf(e.amount),
                            balance = Money.valueOf(balance),
                            number = e.number,
                            memo = e.memo,
                            status = TransactionStatus.parse(e.status),
                            registerAccountId = accountId,
                            transactionAccountId = e.accountId
                    )
                }
    }
}

private class RegisterResultSetHandler(accountId: Long, val initialBalance: Money) : ResultSetHandler {

    companion object {
        private val sql = """
            SELECT
                TRANSACTION_ID,
                TRANSACTION_ACCOUNT_ID,
                TRANSACTION_ACCOUNTS.ACCOUNT_NAME AS TRANSACTION_ACCOUNT_NAME,
                TRANSACTION_DATE,
                TRANSACTION_NUMBER,
                TRANSACTION_MEMO,
                TRANSACTION_STATUS,
                PAYEE_ID,
                PAYEE_NAME,
                TRANSFER_ID,
                TRANSFER_ACCOUNT_ID,
                TRANSFER_ACCOUNTS.ACCOUNT_NAME AS TRANSFER_ACCOUNT_NAME,
                TRANSFER_AMOUNT,
                TRANSFER_NUMBER,
                TRANSFER_MEMO,
                TRANSFER_STATUS,
                TRANSFER_ORDER_IN_TRANSACTION,
                ENTRY_ID,
                ENTRY_AMOUNT,
                ENTRY_ORDER_IN_TRANSACTION,
                CATEGORIES.CATEGORY_ID AS ENTRY_CATEGORY_ID,
                CATEGORIES.CATEGORY_NAME AS ENTRY_CATEGORY_NAME,
                PARENT_CATEGORIES.CATEGORY_NAME AS ENTRY_PARENT_CATEGORY_NAME
            FROM TRANSACTIONS
            LEFT JOIN ACCOUNTS AS TRANSACTION_ACCOUNTS ON TRANSACTIONS.TRANSACTION_ACCOUNT_ID = TRANSACTION_ACCOUNTS.ACCOUNT_ID
            LEFT JOIN PAYEES ON TRANSACTIONS.TRANSACTION_PAYEE_ID = PAYEES.PAYEE_ID
            LEFT JOIN TRANSFERS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
            LEFT JOIN ACCOUNTS AS TRANSFER_ACCOUNTS ON TRANSFERS.TRANSFER_ACCOUNT_ID = TRANSFER_ACCOUNTS.ACCOUNT_ID
            LEFT JOIN ENTRIES ON TRANSACTIONS.TRANSACTION_ID = ENTRIES.ENTRY_TRANSACTION_ID
            LEFT JOIN CATEGORIES ON ENTRIES.ENTRY_CATEGORY_ID = CATEGORIES.CATEGORY_ID
            LEFT JOIN CATEGORIES AS PARENT_CATEGORIES ON CATEGORIES.CATEGORY_PARENT_ID = PARENT_CATEGORIES.CATEGORY_ID
            WHERE TRANSACTIONS.TRANSACTION_ACCOUNT_ID = ? OR TRANSFERS.TRANSFER_ACCOUNT_ID = ?
        """.trimIndent()
    }

    val query = Query(sql, listOf(accountId, accountId))

    private val collector = RegisterCollector(accountId)

    val results: List<RegisterEntry>
        get() = collector.getRegisterEntries(initialBalance)

    override fun accept(rs: ResultSet) {

        while (rs.next()) {

            collector.collect(
                    transactionId = rs.getLong("TRANSACTION_ID"),
                    transactionAccountId = rs.getLong("TRANSACTION_ACCOUNT_ID"),
                    transactionAccountName = rs.getString("TRANSACTION_ACCOUNT_NAME"),
                    transactionDate = rs.getLocalDate("TRANSACTION_DATE"),
                    transactionNumber = rs.getString("TRANSACTION_NUMBER"),
                    transactionMemo = rs.getString("TRANSACTION_MEMO"),
                    transactionStatus = rs.getString("TRANSACTION_STATUS"),
                    payeeId = rs.getLongOrNull("PAYEE_ID"),
                    payeeName = rs.getString("PAYEE_NAME"),
                    transferId = rs.getLongOrNull("TRANSFER_ID"),
                    transferAccountId = rs.getLongOrNull("TRANSFER_ACCOUNT_ID"),
                    transferAccountName = rs.getString("TRANSFER_ACCOUNT_NAME"),
                    transferAmount = rs.getLongOrNull("TRANSFER_AMOUNT"),
                    transferNumber = rs.getString("TRANSFER_NUMBER"),
                    transferMemo = rs.getString("TRANSFER_MEMO"),
                    transferStatus = rs.getString("TRANSFER_STATUS"),
                    transferOrderInTransaction = rs.getIntOrNull("TRANSFER_ORDER_IN_TRANSACTION"),
                    entryId = rs.getLongOrNull("ENTRY_ID"),
                    entryAmount = rs.getLongOrNull("ENTRY_AMOUNT"),
                    entryCategoryId = rs.getLongOrNull("ENTRY_CATEGORY_ID"),
                    entryCategoryName = rs.getString("ENTRY_CATEGORY_NAME"),
                    entryParentCategoryName = rs.getString("ENTRY_PARENT_CATEGORY_NAME"),
                    entryOrderInTransaction = rs.getIntOrNull("ENTRY_ORDER_IN_TRANSACTION")
            )
        }
    }
}

fun Account.getRegister(executor: QueryExecutor): List<RegisterEntry> {

    val accountId = identity ?: throw IllegalStateException("Can't get register for an unsaved account.")
    val handler = RegisterResultSetHandler(accountId, initialBalance ?: Money.zero())

    executor.executeQuery(handler.query, handler)

    return handler.results
}
