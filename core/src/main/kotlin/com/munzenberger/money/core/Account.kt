package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountModel
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

class Account internal constructor(model: AccountModel) : Persistable<AccountModel>(model, AccountTable) {

    constructor() : this(AccountModel())

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    var number: String?
        get() = model.number
        set(value) { model.number = value }

    var accountType: AccountType? = null

    var bank: Bank? = null

    var initialBalance: Money?
        get() = model.initialBalance?.let { Money.valueOf(it) }
        set(value) { model.initialBalance = value?.value }

    fun getBalance(executor: QueryExecutor): Money {
        val collector = AccountBalanceCollector(identity, model.initialBalance)
        executor.executeQuery(collector.query, collector)
        return collector.result
    }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
        model.accountType = accountType.getIdentity(tx)
        model.bank = bank.getIdentity(tx)
        super.save(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, AccountTable, AccountResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, AccountTable, AccountResultSetMapper())
    }
}

class AccountResultSetMapper : ResultSetMapper<Account> {

    override fun apply(resultSet: ResultSet): Account {

        val model = AccountModel().apply {
            AccountTable.getValues(resultSet, this)
        }

        return Account(model).apply {
            accountType = model.accountType?.let { AccountTypeResultSetMapper().apply(resultSet) }
            bank = model.bank?.let { BankResultSetMapper().apply(resultSet) }
        }
    }
}

private class AccountBalanceCollector(private val accountId: Long?, initialBalance: Long?) : ResultSetHandler {

    val sql = """
        SELECT TRANSACTION_ACCOUNT_ID, TRANSFER_ACCOUNT_ID, TRANSFER_AMOUNT, ENTRY_AMOUNT
        FROM TRANSACTIONS
        LEFT JOIN TRANSFERS ON TRANSACTIONS.TRANSACTION_ID = TRANSFERS.TRANSFER_TRANSACTION_ID
        LEFT JOIN ENTRIES ON TRANSACTIONS.TRANSACTION_ID = ENTRIES.ENTRY_TRANSACTION_ID
        WHERE TRANSACTIONS.TRANSACTION_ACCOUNT_ID = ? OR TRANSFERS.TRANSFER_ACCOUNT_ID = ?
    """.trimIndent()

    val query = Query(sql, listOf(accountId, accountId))

    val result: Money
        get() = Money.valueOf(accumulator)

    private var accumulator: Long = initialBalance ?: 0

    override fun accept(rs: ResultSet) {
        while (rs.next()) {

            val transactionAccount = rs.getLong("TRANSACTION_ACCOUNT_ID")
            val transferAccount = rs.getLong("TRANSFER_ACCOUNT_ID")
            val transferAmount = rs.getLongOrNull("TRANSFER_AMOUNT")
            val entryAmount = rs.getLongOrNull("ENTRY_AMOUNT")

            transferAmount?.let { amount ->

                // if the specified account is the parent for the transaction,
                // then the transfer amount is credited (added) to the accumulator
                if (accountId == transactionAccount) {
                    accumulator += amount
                }

                // if the specified account is the child as the transfer,
                // then the amount is debited (subtracted) from the accumulator
                if (accountId == transferAccount) {
                    accumulator -= amount
                }
            }

            entryAmount?.let { amount ->
                accumulator += amount
            }
        }
    }
}
