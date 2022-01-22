package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferEntryTable
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
        SELECT SUM(${TransferEntryTable.amountColumn}) AS TOTAL
        FROM ${TransactionTable.name}
        INNER JOIN ${TransferEntryTable.name} ON ${TransferEntryTable.name}.${TransferEntryTable.transactionColumn} = ${TransactionTable.name}.${TransactionTable.identityColumn}
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
        FROM ${TransferEntryTable.name}
        INNER JOIN ${TransactionTable.name} ON ${TransactionTable.name}.${TransactionTable.identityColumn} = ${TransferEntryTable.name}.${TransferEntryTable.transactionColumn}
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

private class CategoryEntryBalanceCollector(accountId: Long) : AccountBalanceCollector {

    private val sql = """
        SELECT SUM(${CategoryEntryTable.amountColumn}) AS TOTAL
        FROM ${CategoryEntryTable.name}
        INNER JOIN ${TransactionTable.name} ON ${TransactionTable.name}.${TransactionTable.identityColumn} = ${CategoryEntryTable.name}.${CategoryEntryTable.transactionColumn}
        WHERE ${TransactionTable.name}.${TransactionTable.accountColumn} = ?
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
