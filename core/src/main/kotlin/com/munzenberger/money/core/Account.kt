package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountModel
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
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

    override fun save(executor: QueryExecutor): Completable {

        val accountTypeIdentity = Persistable.getIdentity(accountType, executor) { model.accountType = it }
        val bankIdentity = Persistable.getIdentity(bank, executor) { model.bank = it }

        return completableChain(accountTypeIdentity, bankIdentity, super.save(executor))
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, AccountTable, AccountResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, AccountTable, AccountResultSetMapper(), Account::class)
    }
}

class AccountResultSetMapper : ResultSetMapper<Account> {

    override fun map(resultSet: ResultSet): Account {

        val model = AccountModel().apply {
            identity = resultSet.getLong(AccountTable.identityColumn)
            name = resultSet.getString(AccountTable.nameColumn)
            number = resultSet.getString(AccountTable.numberColumn)
            accountType = resultSet.getLongOrNull(AccountTable.accountTypeColumn)
            bank = resultSet.getLongOrNull(AccountTable.bankColumn)
        }

        return Account(model).apply {
            accountType = model.accountType?.let { AccountTypeResultSetMapper().map(resultSet) }
            bank = model.bank?.let { BankResultSetMapper().map(resultSet) }
        }
    }
}
