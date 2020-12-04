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

data class AccountTransaction(
        val transactionId: Long,
        val date: LocalDate,
        val payee: String?,
        val categories: List<Category>,
        val amount: Money,
        val balance: Money,
        val number: String?,
        val memo: String?,
        val status: TransactionStatus,
        private val contextId: Long,
        private val transactionAccountId: Long,
        private val transferCategories: List<TransferAndCategory>
) {
    data class Category(
            val accountTypeCategory: AccountType.Category?,
            val accountName: String?,
            val categoryName: String?
    )

    data class TransferAndCategory(
            val transferId: Long,
            val categoryAccountId: Long
    )

    fun updateStatus(status: TransactionStatus, executor: QueryExecutor) = executor.transaction { tx ->

        if (contextId == transactionAccountId) {
            // this transaction is in the context of a parent account,
            // so update the status of the parent transaction
            val query = Query.update(TransactionTable.name)
                    .set(TransactionTable.statusColumn, status.name)
                    .where(TransactionTable.identityColumn.eq(transactionId))
                    .build()

            tx.executeUpdate(query)
        }

        val transfers = transferCategories
                .filter { it.categoryAccountId == contextId }
                .map { it.transferId }

        if (transfers.isNotEmpty()) {
            // this transaction is in the context of one or more child transfers,
            // so update the status of each transfer
            val query = Query.update(TransferTable.name)
                    .set(TransferTable.statusColumn, status.name)
                    .where(TransferTable.identityColumn.inGroup(transfers))
                    .build()

            tx.executeUpdate(query)
        }
    }
}

private class AccountTransactionCollector(
        private val accountId: Long,
        private val initialBalance: Money
) {

    private class TransactionEntry(
            val transactionId: Long,
            val transactionAccountId: Long,
            val date: LocalDate,
            val payee: String?,
            var amount: Long = 0,
            var categories: List<AccountTransaction.Category> = emptyList(),
            var number: String? = null,
            var memo: String? = null,
            var status: String? = null,
            var transferCategories: List<AccountTransaction.TransferAndCategory> = emptyList()
    )

    data class TransferEntry(
            val transactionId: Long,
            val date: LocalDate,
            val payee: String?,
            val transferAmount: Long,
            val transactionAccountId: Long,
            val transactionAccountTypeCategory: String,
            val transactionAccountName: String,
            val transactionNumber: String?,
            val transactionStatus: String?,
            val categoryAccountId: Long,
            val categoryAccountTypeCategory: String?,
            val categoryAccountName: String?,
            val categoryName: String?,
            val transactionMemo: String?,
            val transferNumber: String?,
            val transferMemo: String?,
            val transferStatus: String?,
            val transferId: Long
    )

    private val entries = mutableMapOf<Long, TransactionEntry>()

    fun collect(t: TransferEntry) {

        val entry = entries.getOrPut(t.transactionId) {
            TransactionEntry(
                    transactionId = t.transactionId,
                    transactionAccountId = t.transactionAccountId,
                    date = t.date,
                    payee = t.payee
            )
        }

        entry.transferCategories += AccountTransaction.TransferAndCategory(
                transferId = t.transferId,
                categoryAccountId = t.categoryAccountId
        )

        if (accountId == t.transactionAccountId) {
            entry.amount += t.transferAmount

            // this is a parent transaction, so use the category of the child transfer
            val category = AccountTransaction.Category(
                    accountTypeCategory = t.categoryAccountTypeCategory?.let { AccountType.Category.valueOf(it) },
                    accountName = t.categoryAccountName,
                    categoryName = t.categoryName
            )

            entry.categories += category
            entry.number = t.transactionNumber
            entry.memo = t.transactionMemo
            entry.status = t.transactionStatus
        }

        if (accountId == t.categoryAccountId) {
            entry.amount -= t.transferAmount

            // this is a child transfer, so use the parent transaction account as the category
            val category = AccountTransaction.Category(
                    accountTypeCategory = AccountType.Category.valueOf(t.transactionAccountTypeCategory),
                    accountName = t.transactionAccountName,
                    categoryName = null
            )

            entry.categories = listOf(category)
            entry.number = t.transferNumber
            entry.memo = t.transferMemo
            entry.status = t.transferStatus
        }
    }

    fun getAccountTransactions(): List<AccountTransaction> {

        var balance = initialBalance.value

        return entries.values
                .sortedBy { it.date }
                .map {
                    balance += it.amount
                    AccountTransaction(
                            transactionId = it.transactionId,
                            date = it.date,
                            payee = it.payee,
                            categories = it.categories,
                            amount = Money.valueOf(it.amount),
                            balance = Money.valueOf(balance),
                            number = it.number,
                            memo = it.memo,
                            status = TransactionStatus.parse(it.status),
                            contextId = accountId,
                            transactionAccountId = it.transactionAccountId,
                            transferCategories = it.transferCategories)
                }
    }
}

private class AccountTransactionResultSetHandler(accountId: Long, initialBalance: Money) : ResultSetHandler {

    companion object {
        private val sql =
                """
                SELECT TRANSACTION_ID,
                    TRANSACTION_DATE,
                    TRANSACTION_NUMBER,
                    TRANSACTION_MEMO,
                    TRANSACTION_STATUS,
                    TRANSFER_ID,
                    TRANSFER_AMOUNT,
                    TRANSFER_NUMBER,
                    TRANSFER_MEMO,
                    TRANSFER_STATUS,
                    PAYEE_NAME,
                    TRANSACTION_ACCOUNT_ID,
                    CATEGORY_ACCOUNT_ID,
                    TRANSACTION_ACCOUNT_TYPE.ACCOUNT_TYPE_CATEGORY AS TRANSACTION_ACCOUNT_TYPE_CATEGORY,
                    TRANSACTION_ACCOUNT.ACCOUNT_NAME AS TRANSACTION_ACCOUNT_NAME,
                    CATEGORY_ACCOUNT_TYPE.ACCOUNT_TYPE_CATEGORY AS CATEGORY_ACCOUNT_TYPE_CATEGORY,
                    CATEGORY_ACCOUNT.ACCOUNT_NAME AS CATEGORY_ACCOUNT_NAME,
                    CATEGORY_NAME
                FROM TRANSACTIONS
                LEFT JOIN ACCOUNTS AS TRANSACTION_ACCOUNT ON TRANSACTIONS.TRANSACTION_ACCOUNT_ID = TRANSACTION_ACCOUNT.ACCOUNT_ID
                LEFT JOIN ACCOUNT_TYPES AS TRANSACTION_ACCOUNT_TYPE ON TRANSACTION_ACCOUNT.ACCOUNT_TYPE_ID = TRANSACTION_ACCOUNT_TYPE.ACCOUNT_TYPE_ID
                LEFT JOIN TRANSFERS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
                LEFT JOIN PAYEES ON TRANSACTIONS.TRANSACTION_PAYEE_ID = PAYEES.PAYEE_ID
                LEFT JOIN CATEGORIES ON TRANSFERS.TRANSFER_CATEGORY_ID = CATEGORIES.CATEGORY_ID
                LEFT JOIN ACCOUNTS AS CATEGORY_ACCOUNT ON CATEGORIES.CATEGORY_ACCOUNT_ID = CATEGORY_ACCOUNT.ACCOUNT_ID
                LEFT JOIN ACCOUNT_TYPES AS CATEGORY_ACCOUNT_TYPE ON CATEGORY_ACCOUNT.ACCOUNT_TYPE_ID = CATEGORY_ACCOUNT_TYPE.ACCOUNT_TYPE_ID
                WHERE TRANSACTION_ACCOUNT_ID = ? OR CATEGORY_ACCOUNT_ID = ?
            """.trimIndent()
    }

    private val collector = AccountTransactionCollector(accountId, initialBalance)

    val query: Query = Query(sql, listOf(accountId, accountId))

    val results: List<AccountTransaction>
        get() = collector.getAccountTransactions()

    override fun accept(rs: ResultSet) {

        while (rs.next()) {

            val t = AccountTransactionCollector.TransferEntry(
                    transactionId = rs.getLong("TRANSACTION_ID"),
                    date = rs.getLocalDate("TRANSACTION_DATE"),
                    transactionAccountId = rs.getLong("TRANSACTION_ACCOUNT_ID"),
                    transactionAccountTypeCategory = rs.getString("TRANSACTION_ACCOUNT_TYPE_CATEGORY"),
                    transactionAccountName = rs.getString("TRANSACTION_ACCOUNT_NAME"),
                    transactionNumber = rs.getString("TRANSACTION_NUMBER"),
                    transactionStatus = rs.getString("TRANSACTION_STATUS"),
                    categoryAccountId = rs.getLong("CATEGORY_ACCOUNT_ID"),
                    categoryAccountTypeCategory = rs.getString("CATEGORY_ACCOUNT_TYPE_CATEGORY"),
                    categoryAccountName = rs.getString("CATEGORY_ACCOUNT_NAME"),
                    categoryName = rs.getString("CATEGORY_NAME"),
                    transferAmount = rs.getLong("TRANSFER_AMOUNT"),
                    payee = rs.getString("PAYEE_NAME"),
                    transactionMemo = rs.getString("TRANSACTION_MEMO"),
                    transferNumber = rs.getString("TRANSFER_NUMBER"),
                    transferMemo = rs.getString("TRANSFER_MEMO"),
                    transferStatus = rs.getString("TRANSFER_STATUS"),
                    transferId = rs.getLong("TRANSFER_ID")
            )

            collector.collect(t)
        }
    }
}

fun Account.getAccountTransactions(database: MoneyDatabase): List<AccountTransaction> {

    val accountId = identity ?: throw IllegalStateException("Can't get transactions for an unsaved account.")
    val handler = AccountTransactionResultSetHandler(accountId, initialBalance ?: Money.zero())

    database.executeQuery(handler.query, handler)

    return handler.results
}
