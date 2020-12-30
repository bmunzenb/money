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
        SELECT SUM(TRANSFER_AMOUNT) AS TOTAL
        FROM TRANSACTIONS
        INNER JOIN TRANSFERS ON TRANSFERS.TRANSFER_TRANSACTION_ID = TRANSACTIONS.TRANSACTION_ID
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

private class TransferBalanceCollector(accountId: Long) : AccountBalanceCollector {

    private val sql = """
        SELECT -SUM(TRANSFER_AMOUNT) AS TOTAL
        FROM TRANSFERS
        INNER JOIN TRANSACTIONS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
        WHERE TRANSFER_ACCOUNT_ID = ?
    """.trimIndent()

    override val query = Query(sql, listOf(accountId))

    override val result: Long
        get() = total

    private var total: Long = 0

    override fun accept(rs: ResultSet) {
        total = rs.getLong("TOTAL")
    }
}

private class EntryBalanceCollector(accountId: Long) : AccountBalanceCollector {

    private val sql = """
        SELECT SUM(ENTRY_AMOUNT) AS TOTAL
        FROM ENTRIES
        INNER JOIN TRANSACTIONS ON TRANSACTIONS.TRANSACTION_ID = ENTRIES.ENTRY_TRANSACTION_ID
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
            TransferBalanceCollector(accountId),
            EntryBalanceCollector(accountId)
    )

    val totals = collectors.map {
        executor.executeQuery(it.query, it)
        it.result
    }

    val balance = initialBalance + totals.sum()

    return Money.valueOf(balance)
}
