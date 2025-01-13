package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountModel
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.sql.OrderableQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

data class AccountIdentity(override val value: Long) : Identity

class Account internal constructor(model: AccountModel) : AbstractMoneyEntity<AccountIdentity, AccountModel>(model, AccountTable) {
    constructor() : this(AccountModel())

    override val identity: AccountIdentity?
        get() = model.identity?.let { AccountIdentity(it) }

    var name: String?
        get() = model.name
        set(value) {
            model.name = value
        }

    var number: String?
        get() = model.number
        set(value) {
            model.number = value
        }

    var accountType: AccountType? = null

    var bank: Bank? = null

    var initialBalance: Money?
        get() = model.initialBalance?.let { Money.valueOf(it) }
        set(value) {
            model.initialBalance = value?.value
        }

    override fun save(executor: QueryExecutor) =
        executor.transaction { tx ->
            model.accountType = accountType?.getAutoSavedIdentity(tx)?.value
            model.bank = bank?.getAutoSavedIdentity(tx)?.value
            super.save(tx)
        }

    companion object {
        fun find(
            executor: QueryExecutor,
            block: OrderableQueryBuilder<*>.() -> Unit = {},
        ) = find(executor, AccountTable, AccountResultSetMapper, block)

        fun get(
            identity: AccountIdentity,
            executor: QueryExecutor,
        ) = get(identity, executor, AccountTable, AccountResultSetMapper)
    }
}

object AccountResultSetMapper : ResultSetMapper<Account> {
    override fun apply(resultSet: ResultSet): Account {
        val model = AccountTable.getValues(resultSet, AccountModel())

        return Account(model).apply {
            accountType = model.accountType?.let { AccountTypeResultSetMapper.apply(resultSet) }
            bank = model.bank?.let { BankResultSetMapper.apply(resultSet) }
        }
    }
}
