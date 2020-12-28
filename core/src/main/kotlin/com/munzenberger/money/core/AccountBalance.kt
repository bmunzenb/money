package com.munzenberger.money.core

import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import java.sql.ResultSet

internal interface AccountBalanceCollector : ResultSetHandler {

    val query: Query

    val result: Long
}

internal class TransferBalanceCollector(private val accountId: Long?) : AccountBalanceCollector {

    private val sql = """
        SELECT TRANSACTION_ACCOUNT_ID, TRANSFER_ACCOUNT_ID, TRANSFER_AMOUNT
        FROM TRANSFERS
        INNER JOIN TRANSACTIONS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
        WHERE TRANSACTIONS.TRANSACTION_ACCOUNT_ID = ? OR TRANSFERS.TRANSFER_ACCOUNT_ID = ?
    """.trimIndent()

    override val query = Query(sql, listOf(accountId, accountId))

    private var accumulator: Long = 0

    override val result: Long
        get() = accumulator

    override fun accept(rs: ResultSet) {
        while (rs.next()) {

            val transactionAccountId = rs.getLong("TRANSACTION_ACCOUNT_ID")
            val transferAccountId = rs.getLong("TRANSFER_ACCOUNT_ID")
            val transferAmount = rs.getLong("TRANSFER_AMOUNT")

            // if the specified account is the parent for the transaction,
            // then the transfer amount is credited (added) to the accumulator
            if (accountId == transactionAccountId) {
                accumulator += transferAmount
            }

            // if the specified account is the child as the transfer,
            // then the amount is debited (subtracted) from the accumulator
            if (accountId == transferAccountId) {
                accumulator -= transferAmount
            }
        }
    }
}

internal class EntryBalanceCollector(accountId: Long?) : AccountBalanceCollector {

    private val sql = """
        SELECT SUM(ENTRY_AMOUNT) AS TOTAL
        FROM ENTRIES
        INNER JOIN TRANSACTIONS ON TRANSACTIONS.TRANSACTION_ID = ENTRIES.ENTRY_TRANSACTION_ID
        WHERE TRANSACTIONS.TRANSACTION_ACCOUNT_ID = ?
    """.trimIndent()

    override val query = Query(sql, listOf(accountId))

    private var accumulator: Long = 0

    override val result: Long
        get() = accumulator

    override fun accept(rs: ResultSet) {
        while (rs.next()) {
            accumulator += rs.getLong("TOTAL")
        }
    }
}

fun Account.getBalance(executor: QueryExecutor): Money {

    val initialBalance: Long = initialBalance?.value ?: 0

    val balance = listOf(
            TransferBalanceCollector(identity),
            EntryBalanceCollector(identity)
    ).map {
        executor.executeQuery(it.query, it)
        it.result
    }.fold(initialBalance) { acc, v -> acc + v }

    return Money.valueOf(balance)
}
