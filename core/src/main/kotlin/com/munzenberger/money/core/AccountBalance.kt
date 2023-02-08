package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferEntryTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import java.sql.ResultSet

private interface AccountBalanceCollector : ResultSetHandler {

    val query: Query

    val result: Long
}

private class TransactionTransferEntryBalanceCollector(accountId: Long) : AccountBalanceCollector {

    private val sql = """
        SELECT SUM(${TransferEntryTable.amountColumn}) AS TOTAL
        FROM ${TransactionTable.tableName}
        INNER JOIN ${TransferEntryTable.tableName} ON ${TransferEntryTable.tableName}.${TransferEntryTable.transactionColumn} = ${TransactionTable.tableName}.${TransactionTable.identityColumn}
        WHERE ${TransactionTable.accountColumn} = ?
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
        SELECT -SUM(${TransferEntryTable.amountColumn}) AS TOTAL
        FROM ${TransferEntryTable.tableName}
        WHERE ${TransferEntryTable.accountColumn} = ?
    """.trimIndent()

    override val query = Query(sql, listOf(accountId))

    override val result: Long
        get() = total

    private var total: Long = 0

    override fun accept(rs: ResultSet) {
        total = rs.getLong("TOTAL")
    }
}

private class TransactionCategoryEntryBalanceCollector(accountId: Long) : AccountBalanceCollector {

    private val sql = """
        SELECT SUM(${CategoryEntryTable.amountColumn}) AS TOTAL
        FROM ${CategoryEntryTable.tableName}
        INNER JOIN ${TransactionTable.tableName} ON ${TransactionTable.tableName}.${TransactionTable.identityColumn} = ${CategoryEntryTable.tableName}.${CategoryEntryTable.transactionColumn}
        WHERE ${TransactionTable.tableName}.${TransactionTable.accountColumn} = ?
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

    val accountId = identity ?: error("Can't get balance for an unsaved account.")

    val initialBalance: Long = initialBalance?.value ?: 0

    val collectors = listOf(
            TransactionTransferEntryBalanceCollector(accountId),
            TransferEntryBalanceCollector(accountId),
            TransactionCategoryEntryBalanceCollector(accountId)
    )

    val totals = collectors.map {
        executor.executeQuery(it.query, it)
        it.result
    }

    val balance = initialBalance + totals.sum()

    return Money.valueOf(balance)
}
