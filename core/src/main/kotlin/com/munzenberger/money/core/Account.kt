package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountModel
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
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
