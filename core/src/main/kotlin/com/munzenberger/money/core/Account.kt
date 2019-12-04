package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountModel
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.Condition
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
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

    fun balance(executor: QueryExecutor): Money {

        val creditsQuery = Query.selectFrom(TransferTable.name)
                .cols("SUM(${TransferTable.amountColumn}) AS CREDITS")
                .innerJoin(TransferTable.name, TransferTable.transactionColumn, TransactionTable.name, TransactionTable.identityColumn)
                .where(Condition.eq(TransactionTable.accountColumn, identity))
                .build()

        val credits = executor.getFirst(creditsQuery, object : ResultSetMapper<Long> {
            override fun apply(resultSet: ResultSet) = resultSet.getLong("CREDITS")
        })

        val debitsQuery = Query.selectFrom(TransferTable.name)
                .cols("SUM(${TransferTable.amountColumn}) AS DEBITS")
                .innerJoin(TransferTable.name, TransferTable.categoryColumn, CategoryTable.name, CategoryTable.identityColumn)
                .where(Condition.eq(CategoryTable.accountColumn, identity))
                .build()

        val debits = executor.getFirst(debitsQuery, object : ResultSetMapper<Long> {
            override fun apply(resultSet: ResultSet) = resultSet.getLong("DEBITS")
        })

        val balance = (model.initialBalance ?: 0) + (credits ?: 0) - (debits ?: 0)

        return Money.valueOf(balance)
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
