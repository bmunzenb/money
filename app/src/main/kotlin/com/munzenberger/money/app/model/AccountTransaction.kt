package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.getLongOrNull
import java.sql.ResultSet
import java.util.Date

data class AccountTransaction(
        val transactionId: Long,
        val date: Date,
        val payee: String?,
        val amount: Money,
        val balance: Money
)

private class AccountTransactionCollector(
        private val accountId: Long,
        private val initialBalance: Money
) {

    private class Entry(
            val transactionId: Long,
            val date: Date,
            val payee: String?,
            var amount: Long = 0
    )

    private val map = mutableMapOf<Long, Entry>()

    fun collect(
            transactionId: Long,
            date: Date,
            payee: String?,
            transferAmount: Long,
            transactionAccountId: Long,
            categoryAccountId: Long?) {

        var entry = map[transactionId]

        if (entry == null) {
            entry = Entry(transactionId, date, payee)
            map[transactionId] = entry
        }

        if (accountId == transactionAccountId) {
            entry.amount += transferAmount
        }

        if (accountId == categoryAccountId) {
            entry.amount -= transferAmount
        }
    }

    fun getAccountTransactions(): List<AccountTransaction> {

        var balance = initialBalance.value

        return map.values
                .sortedBy { it.date }
                .map {
                    balance += it.amount
                    AccountTransaction(
                            it.transactionId,
                            it.date,
                            it.payee,
                            Money.valueOf(it.amount),
                            Money.valueOf(balance))
                }
    }
}

private class AccountTransactionResultSetHandler(accountId: Long, initialBalance: Money) : ResultSetHandler {

    companion object {
        val sql =
                """
                SELECT TRANSACTION_ID, TRANSACTION_DATE, TRANSACTION_ACCOUNT_ID, CATEGORY_ACCOUNT_ID, TRANSFER_AMOUNT, PAYEE_NAME
                FROM TRANSACTIONS
                LEFT JOIN TRANSFERS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
                LEFT JOIN PAYEES ON TRANSACTIONS.TRANSACTION_PAYEE_ID = PAYEES.PAYEE_ID
                LEFT JOIN CATEGORIES ON TRANSFERS.TRANSFER_CATEGORY_ID = CATEGORIES.CATEGORY_ID
                WHERE TRANSACTION_ACCOUNT_ID = ? OR CATEGORY_ACCOUNT_ID = ?
            """.trimIndent()
    }

    private val collector = AccountTransactionCollector(accountId, initialBalance)

    val results: List<AccountTransaction>
        get() = collector.getAccountTransactions()

    override fun accept(rs: ResultSet) {

        while (rs.next()) {

            val transactionId = rs.getLong("TRANSACTION_ID")
            val date = rs.getDate("TRANSACTION_DATE")
            val transactionAccountId = rs.getLong("TRANSACTION_ACCOUNT_ID")
            val categoryAccountId = rs.getLongOrNull("CATEGORY_ACCOUNT_ID")
            val transferAmount = rs.getLong("TRANSFER_AMOUNT")
            val payee = rs.getString("PAYEE_NAME")

            collector.collect(transactionId, date, payee, transferAmount, transactionAccountId, categoryAccountId)
        }
    }
}

fun Account.getAccountTransactions(database: MoneyDatabase): List<AccountTransaction> {

    val accountId = identity ?: throw IllegalStateException("Can't get transactions for an unsaved account.")

    val handler = AccountTransactionResultSetHandler(accountId, initialBalance ?: Money.zero())

    Query(AccountTransactionResultSetHandler.sql, listOf(accountId, accountId)).let {
        database.executeQuery(it, handler)
    }

    return handler.results
}
