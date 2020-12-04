package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.eq
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
        val categories: List<Category>,
        val amount: Money,
        val balance: Money,
        val memo: String?,
        val number: String?,
        val status: TransactionStatus,
        private val registerAccountId: Long,
        private val transactionAccountId: Long,
        private val transfers: List<Transfer>,
) {
    data class Category(
            val accountId: Long,
            val accountName: String,
            val accountIsCategory: Boolean
    )

    data class Transfer(
            val transferId: Long,
            val accountId: Long
    )

    fun updateStatus(status: TransactionStatus, executor: QueryExecutor) = executor.transaction { tx ->

        if (registerAccountId == transactionAccountId) {
            // update the status of the parent transaction
            val query = Query.update(TransactionTable.name)
                    .set(TransactionTable.statusColumn, status.name)
                    .where(TransactionTable.identityColumn.eq(transactionId))
                    .build()

            tx.executeUpdate(query)
        }

        val ts = transfers
                .filter { registerAccountId == it.accountId }
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
            var categories: List<RegisterEntry.Category> = emptyList(),
            var transfers: List<RegisterEntry.Transfer> = emptyList()
    )

    private val entries = mutableMapOf<Long, TransactionEntry>()

    fun collect(
            transactionId: Long,
            transactionAccountId: Long,
            transactionAccountName: String,
            transactionDate: LocalDate,
            transactionNumber: String?,
            transactionMemo: String,
            transactionStatus: String,
            payeeId: Long?,
            payeeName: String?,
            transferId: Long,
            transferAccountId: Long,
            transferAccountName: String,
            transferAccountIsCategory: Boolean,
            transferAmount: Long,
            transferNumber: String?,
            transferMemo: String?,
            transferStatus: String
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

        entry.transfers += RegisterEntry.Transfer(
                transferId = transferId,
                accountId = transferAccountId
        )

        if (accountId == transactionAccountId) {
            // this is a parent transaction, so all transfer amounts are
            // credited (added) to the total amount
            entry.amount += transferAmount
            entry.status = transactionStatus
            entry.number = transactionNumber
            entry.memo = transactionMemo

            // use the constituent transfers as the categories
            entry.categories += RegisterEntry.Category(
                    accountId = transferAccountId,
                    accountName = transferAccountName,
                    accountIsCategory = transferAccountIsCategory
            )
        }

        if (accountId == transferAccountId) {
            // this is a child transfer to a larger transaction, so all
            // transfer amounts are debited (subtracted) from the total amount
            entry.amount -= transferAmount
            entry.status = transferStatus
            entry.number = transferNumber
            entry.memo = transferMemo

            // use the parent transaction as the category
            entry.categories = listOf(
                    RegisterEntry.Category(
                            accountId = transactionAccountId,
                            accountName = transactionAccountName,
                            // assumes this is a transfer from a non-category account
                            accountIsCategory = false
                    )
            )
        }
    }

    fun getRegisterEntries(initialBalance: Money): List<RegisterEntry> {

        var balance = initialBalance.value

        return entries.values
                .sortedBy { it.date }
                .map {
                    balance += it.amount
                    RegisterEntry(
                            transactionId = it.transactionId,
                            date = it.date,
                            payeeId = it.payeeId,
                            payeeName = it.payeeName,
                            categories = it.categories,
                            amount = Money.valueOf(it.amount),
                            balance = Money.valueOf(balance),
                            number = it.number,
                            memo = it.memo,
                            status = TransactionStatus.parse(it.status),
                            registerAccountId = accountId,
                            transactionAccountId = it.accountId,
                            transfers = it.transfers
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
                TRANSFER_ACCOUNT_TYPES.ACCOUNT_TYPE_IS_CATEGORY AS TRANSFER_ACCOUNT_IS_CATEGORY,
                TRANSFER_AMOUNT,
                TRANSFER_NUMBER,
                TRANSFER_MEMO,
                TRANSFER_STATUS
            FROM TRANSACTIONS
            LEFT JOIN TRANSFERS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
            LEFT JOIN PAYEES ON TRANSACTIONS.TRANSACTION_PAYEE_ID = PAYEES.PAYEE_ID
            LEFT JOIN ACCOUNTS AS TRANSACTION_ACCOUNTS ON TRANSACTIONS.TRANSACTION_ACCOUNT_ID = TRANSACTION_ACCOUNTS.ACCOUNT_ID
            LEFT JOIN ACCOUNTS AS TRANSFER_ACCOUNTS ON TRANSFERS.TRANSFER_ACCOUNT_ID = TRANSFER_ACCOUNTS.ACCOUNT_ID
            LEFT JOIN ACCOUNT_TYPES AS TRANSFER_ACCOUNT_TYPES ON TRANSFER_ACCOUNTS.ACCOUNT_TYPE_ID = TRANSFER_ACCOUNT_TYPES.ACCOUNT_TYPE_ID
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
                    transferId = rs.getLong("TRANSFER_ID"),
                    transferAccountId = rs.getLong("TRANSFER_ACCOUNT_ID"),
                    transferAccountName = rs.getString("TRANSFER_ACCOUNT_NAME"),
                    transferAccountIsCategory = rs.getBoolean("TRANSFER_ACCOUNT_IS_CATEGORY"),
                    transferAmount = rs.getLong("TRANSFER_AMOUNT"),
                    transferNumber = rs.getString("TRANSFER_NUMBER"),
                    transferMemo = rs.getString("TRANSFER_MEMO"),
                    transferStatus = rs.getString("TRANSFER_STATUS")
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
