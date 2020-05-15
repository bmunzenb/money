package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountModel
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.Condition
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.eq
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

    fun balance(executor: QueryExecutor): Money {
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
            identity = resultSet.getLong(AccountTable.identityColumn)
            name = resultSet.getString(AccountTable.nameColumn)
            number = resultSet.getString(AccountTable.numberColumn)
            accountType = resultSet.getLongOrNull(AccountTable.accountTypeColumn)
            bank = resultSet.getLongOrNull(AccountTable.bankColumn)
            initialBalance = resultSet.getLongOrNull(AccountTable.initialBalanceColumn)
        }

        return Account(model).apply {
            accountType = model.accountType?.let { AccountTypeResultSetMapper().apply(resultSet) }
            bank = model.bank?.let { BankResultSetMapper().apply(resultSet) }
        }
    }
}

private class AccountBalanceCollector(private val accountId: Long?, initialBalance: Long?) : ResultSetHandler {

    val query: Query = Query.selectFrom(TransferTable.name)
            .cols(TransferTable.amountColumn, CategoryTable.accountColumn)
            .innerJoin(TransferTable.name, TransferTable.transactionColumn, TransactionTable.name, TransactionTable.identityColumn)
            .innerJoin(TransferTable.name, TransferTable.categoryColumn, CategoryTable.name, CategoryTable.identityColumn)
            .where(TransactionTable.accountColumn.eq(accountId).or(CategoryTable.accountColumn.eq(accountId)))
            .build()

    val result: Money
        get() = Money.valueOf(accumulator)

    private var accumulator: Long = initialBalance ?: 0

    override fun accept(rs: ResultSet) {
        while (rs.next()) {

            val amount = rs.getLong(TransferTable.amountColumn)
            val categoryAccount = rs.getLong(CategoryTable.accountColumn)

            accumulator += when (accountId) {
                categoryAccount -> -amount
                else -> amount
            }
        }
    }
}
