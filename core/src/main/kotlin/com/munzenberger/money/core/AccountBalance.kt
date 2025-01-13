package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferEntryTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetConsumer
import java.sql.ResultSet

private interface AccountBalanceCollector : ResultSetConsumer {
    val query: Query

    val result: Long
}

private class TransactionTransferEntryBalanceCollector(
    accountId: AccountIdentity,
) : AccountBalanceCollector {
    private val sql =
        """
        SELECT SUM(${TransferEntryTable.TRANSFER_ENTRY_AMOUNT}) AS TOTAL
        FROM ${TransactionTable.tableName}
        INNER JOIN ${TransferEntryTable.tableName} ON ${TransferEntryTable.tableName}.${TransferEntryTable.TRANSFER_ENTRY_TRANSACTION_ID} = ${TransactionTable.tableName}.${TransactionTable.identityColumn}
        WHERE ${TransactionTable.TRANSACTION_ACCOUNT_ID} = ?
        """.trimIndent()

    override val query = Query(sql, listOf(accountId.value))

    override val result: Long
        get() = total

    private var total: Long = 0

    override fun accept(rs: ResultSet) {
        if (rs.next()) {
            total = rs.getLong("TOTAL")
        }
    }
}

private class TransferEntryBalanceCollector(
    accountId: AccountIdentity,
) : AccountBalanceCollector {
    private val sql =
        """
        SELECT -SUM(${TransferEntryTable.TRANSFER_ENTRY_AMOUNT}) AS TOTAL
        FROM ${TransferEntryTable.tableName}
        WHERE ${TransferEntryTable.TRANSFER_ENTRY_ACCOUNT_ID} = ?
        """.trimIndent()

    override val query = Query(sql, listOf(accountId.value))

    override val result: Long
        get() = total

    private var total: Long = 0

    override fun accept(rs: ResultSet) {
        if (rs.next()) {
            total = rs.getLong("TOTAL")
        }
    }
}

private class TransactionCategoryEntryBalanceCollector(
    accountId: AccountIdentity,
) : AccountBalanceCollector {
    private val sql =
        """
        SELECT SUM(${CategoryEntryTable.CATEGORY_ENTRY_AMOUNT}) AS TOTAL
        FROM ${CategoryEntryTable.tableName}
        INNER JOIN ${TransactionTable.tableName} ON ${TransactionTable.tableName}.${TransactionTable.identityColumn} = ${CategoryEntryTable.tableName}.${CategoryEntryTable.CATEGORY_ENTRY_TRANSACTION_ID}
        WHERE ${TransactionTable.tableName}.${TransactionTable.TRANSACTION_ACCOUNT_ID} = ?
        """.trimIndent()

    override val query = Query(sql, listOf(accountId.value))

    override val result: Long
        get() = total

    private var total: Long = 0

    override fun accept(rs: ResultSet) {
        if (rs.next()) {
            total = rs.getLong("TOTAL")
        }
    }
}

fun Account.getBalance(executor: QueryExecutor): Money {
    val accountId = identity ?: error("Can't get balance for an unsaved account.")

    val initialBalance: Long = initialBalance?.value ?: 0

    val collectors =
        listOf(
            TransactionTransferEntryBalanceCollector(accountId),
            TransferEntryBalanceCollector(accountId),
            TransactionCategoryEntryBalanceCollector(accountId),
        )

    val totals =
        collectors.map {
            executor.executeQuery(it.query, it)
            it.result
        }

    val balance = initialBalance + totals.sum()

    return Money.valueOf(balance)
}
