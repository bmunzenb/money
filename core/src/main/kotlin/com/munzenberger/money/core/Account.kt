package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountModel
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet

class Account(executor: QueryExecutor, model: AccountModel = AccountModel()) : Persistable<AccountModel>(model, AccountTable, executor) {

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    var number: String?
        get() = model.number
        set(value) { model.number = value }

    var accountType: AccountType? = null

    var bank: Bank? = null

    override fun save(): Completable {

        val accountTypeIdentity = Persistable.getIdentity(accountType) { model.accountType = it }
        val bankIdentity = Persistable.getIdentity(bank) { model.bank = it }

        return accountTypeIdentity.andThen(bankIdentity).andThen(super.save())
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, AccountTable, AccountResultSetMapper(executor))

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, AccountTable, AccountResultSetMapper(executor), Account::class)
    }
}

class AccountResultSetMapper(private val executor: QueryExecutor) : ResultSetMapper<Account> {

    override fun map(resultSet: ResultSet): Account {

        val model = AccountModel().apply {
            identity = resultSet.getLong(AccountTable.identityColumn)
            name = resultSet.getString(AccountTable.nameColumn)
            number = resultSet.getString(AccountTable.numberColumn)
            accountType = resultSet.getLongOrNull(AccountTable.accountTypeColumn)
            bank = resultSet.getLongOrNull(AccountTable.bankColumn)
        }

        return Account(executor, model).apply {
            accountType = model.accountType?.let { AccountTypeResultSetMapper(executor).map(resultSet) }
            bank = model.bank?.let { BankResultSetMapper(executor).map(resultSet) }
        }
    }
}
