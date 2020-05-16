package com.munzenberger.money.core

import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.getLocalDate
import com.munzenberger.money.sql.getLongOrNull
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
        val memo: String?
) {
    data class Category(
            val accountTypeCategory: AccountType.Category?,
            val accountName: String?,
            val categoryName: String?
    )
}

private class AccountTransactionCollector(
        private val accountId: Long,
        private val initialBalance: Money
) {

    private class Entry(
            val transactionId: Long,
            val date: LocalDate,
            val payee: String?,
            var amount: Long = 0,
            var categories: List<AccountTransaction.Category> = emptyList(),
            var number: String? = null,
            var memo: String? = null
    )

    private val entries = mutableMapOf<Long, Entry>()

    fun collect(
            transactionId: Long,
            date: LocalDate,
            payee: String?,
            transferAmount: Long,
            transactionAccountId: Long,
            transactionAccountTypeCategory: String,
            transactionAccountName: String,
            transactionNumber: String?,
            categoryAccountId: Long?,
            categoryAccountTypeCategory: String?,
            categoryAccountName: String?,
            categoryName: String?,
            transactionMemo: String?,
            transferNumber: String?,
            transferMemo: String?
    ) {

        var entry = entries[transactionId]

        if (entry == null) {
            entry = Entry(transactionId, date, payee)
            entries[transactionId] = entry
        }

        if (accountId == transactionAccountId) {
            entry.amount += transferAmount

            // this is a parent transaction, so use the category of the child transfer
            val category = AccountTransaction.Category(
                    accountTypeCategory = categoryAccountTypeCategory?.let { AccountType.Category.valueOf(it) },
                    accountName = categoryAccountName,
                    categoryName = categoryName
            )

            entry.categories += category
            entry.number = transactionNumber
            entry.memo = transactionMemo
        }

        if (accountId == categoryAccountId) {
            entry.amount -= transferAmount

            // this is a child transfer, so use the parent transaction account as the category
            val category = AccountTransaction.Category(
                    accountTypeCategory = AccountType.Category.valueOf(transactionAccountTypeCategory),
                    accountName = transactionAccountName,
                    categoryName = null
            )

            entry.categories += category
            entry.number = transferNumber
            entry.memo = transferMemo
        }
    }

    fun getAccountTransactions(): List<AccountTransaction> {

        var balance = initialBalance.value

        return entries.values
                .sortedBy { it.date }
                .map {
                    balance += it.amount
                    AccountTransaction(
                            it.transactionId,
                            it.date,
                            it.payee,
                            it.categories,
                            Money.valueOf(it.amount),
                            Money.valueOf(balance),
                            it.number,
                            it.memo)
                }
    }
}

private class AccountTransactionResultSetHandler(accountId: Long, initialBalance: Money) : ResultSetHandler {

    companion object {
        private val sql =
                """
                SELECT TRANSACTION_ID, TRANSACTION_DATE, TRANSACTION_NUMBER, TRANSACTION_MEMO, TRANSFER_AMOUNT, TRANSFER_NUMBER, TRANSFER_MEMO, PAYEE_NAME, TRANSACTION_ACCOUNT_ID, CATEGORY_ACCOUNT_ID,
                    TRANSACTION_ACCOUNT_TYPE.ACCOUNT_TYPE_CATEGORY AS TRANSACTION_ACCOUNT_TYPE_CATEGORY, TRANSACTION_ACCOUNT.ACCOUNT_NAME AS TRANSACTION_ACCOUNT_NAME,
                    CATEGORY_ACCOUNT_TYPE.ACCOUNT_TYPE_CATEGORY AS CATEGORY_ACCOUNT_TYPE_CATEGORY, CATEGORY_ACCOUNT.ACCOUNT_NAME AS CATEGORY_ACCOUNT_NAME, CATEGORY_NAME
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

            val transactionId = rs.getLong("TRANSACTION_ID")
            val date = rs.getLocalDate("TRANSACTION_DATE")
            val transactionAccountId = rs.getLong("TRANSACTION_ACCOUNT_ID")
            val transactionAccountTypeCategory = rs.getString("TRANSACTION_ACCOUNT_TYPE_CATEGORY")
            val transactionAccountName = rs.getString("TRANSACTION_ACCOUNT_NAME")
            val transactionNumber = rs.getString("TRANSACTION_NUMBER")
            val categoryAccountId = rs.getLongOrNull("CATEGORY_ACCOUNT_ID")
            val categoryAccountTypeCategory = rs.getString("CATEGORY_ACCOUNT_TYPE_CATEGORY")
            val categoryAccountName = rs.getString("CATEGORY_ACCOUNT_NAME")
            val categoryName = rs.getString("CATEGORY_NAME")
            val transferAmount = rs.getLong("TRANSFER_AMOUNT")
            val payee = rs.getString("PAYEE_NAME")
            val transactionMemo = rs.getString("TRANSACTION_MEMO")
            val transferNumber = rs.getString("TRANSFER_NUMBER")
            val transferMemo = rs.getString("TRANSFER_MEMO")

            collector.collect(
                    transactionId = transactionId,
                    date = date,
                    payee = payee,
                    transferAmount = transferAmount,
                    transactionAccountId = transactionAccountId,
                    transactionAccountTypeCategory = transactionAccountTypeCategory,
                    transactionAccountName = transactionAccountName,
                    transactionNumber = transactionNumber,
                    categoryAccountId = categoryAccountId,
                    categoryAccountTypeCategory = categoryAccountTypeCategory,
                    categoryAccountName = categoryAccountName,
                    categoryName = categoryName,
                    transactionMemo = transactionMemo,
                    transferNumber = transferNumber,
                    transferMemo = transferMemo
            )
        }
    }
}

fun Account.getAccountTransactions(database: MoneyDatabase): List<AccountTransaction> {

    val accountId = identity ?: throw IllegalStateException("Can't get transactions for an unsaved account.")
    val handler = AccountTransactionResultSetHandler(accountId, initialBalance ?: Money.zero())

    database.executeQuery(handler.query, handler)

    return handler.results
}
