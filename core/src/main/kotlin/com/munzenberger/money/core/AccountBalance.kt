package com.munzenberger.money.core

import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import java.lang.IllegalStateException
import java.sql.ResultSet

private interface AccountBalanceCollector : ResultSetHandler {

    val query: Query

    val result: Long
}

private class TransactionBalanceCollector(accountId: Long) : AccountBalanceCollector {

    private val sql = """
        SELECT SUM(TRANSFER_ENTRY_AMOUNT) AS TOTAL
        FROM TRANSACTIONS
        INNER JOIN TRANSFER_ENTRIES ON TRANSFER_ENTRIES.TRANSFER_ENTRY_TRANSACTION_ID = TRANSACTIONS.TRANSACTION_ID
        WHERE TRANSACTION_ACCOUNT_ID = ?
    """.trimIndent()

    override val query = Query(sql, listOf(accountId))

    override val result: Long
        get() = total

    private var total: Long = 0

    override fun accept(rs: ResultSet) {
        total = rs.getLong("TOTAL")
    }
}

private class TransferEntryBalanceCollector(accountId: Long) : AccountBalanceCollector {

    private val sql = """
        SELECT -SUM(TRANSFER_ENTRY_AMOUNT) AS TOTAL
        FROM TRANSFER_ENTRIES
        INNER JOIN TRANSACTIONS ON TRANSACTIONS.TRANSACTION_ID = TRANSFER_ENTRIES.TRANSFER_ENTRY_TRANSACTION_ID
        WHERE TRANSFER_ENTRY_ACCOUNT_ID = ?
    """.trimIndent()

    override val query = Query(sql, listOf(accountId))

    override val result: Long
        get() = total

    private var total: Long = 0

    override fun accept(rs: ResultSet) {
        total = rs.getLong("TOTAL")
    }
}

private class CategoryEntryBalanceCollector(accountId: Long) : AccountBalanceCollector {

    private val sql = """
        SELECT SUM(CATEGORY_ENTRY_AMOUNT) AS TOTAL
        FROM CATEGORY_ENTRIES
        INNER JOIN TRANSACTIONS ON TRANSACTIONS.TRANSACTION_ID = CATEGORY_ENTRIES.CATEGORY_ENTRY_TRANSACTION_ID
        WHERE TRANSACTIONS.TRANSACTION_ACCOUNT_ID = ?
    """.trimIndent()

    override val query = Query(sql, listOf(accountId))

    override val result: Long
        get() = total

    private var total: Long = 0

    override fun accept(rs: ResultSet) {
        total = rs.getLong("TOTAL")
    }
}

fun Account.getBalance(executor: QueryExecutor): Money {

    val accountId = identity ?: throw IllegalStateException("Can't get balance for an unsaved account.")

    val initialBalance: Long = initialBalance?.value ?: 0

    val collectors = listOf(
            TransactionBalanceCollector(accountId),
            TransferEntryBalanceCollector(accountId),
            CategoryEntryBalanceCollector(accountId)
    )

    val totals = collectors.map {
        executor.executeQuery(it.query, it)
        it.result
    }

    val balance = initialBalance + totals.sum()

    return Money.valueOf(balance)
}
