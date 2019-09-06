package com.munzenberger.money.core

import com.munzenberger.money.core.model.*
import com.munzenberger.money.sql.*
import io.reactivex.Completable
import io.reactivex.Single
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

    fun balance(executor: QueryExecutor) = Single.fromCallable {

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

        val balance = (credits ?: 0) - (debits ?: 0)

        Money.valueOf(balance)
    }

    override fun save(executor: QueryExecutor): Completable {

        val tx = executor.createTransaction()

        val getAccountTypeIdentity = getIdentity(accountType, tx) { model.accountType = it }
        val getBankIdentity = getIdentity(bank, tx) { model.bank = it }

        return Completable.concatArray(getAccountTypeIdentity, getBankIdentity, super.save(tx)).withTransaction(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, AccountTable, AccountResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, AccountTable, AccountResultSetMapper(), Account::class)
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
        }

        return Account(model).apply {
            accountType = model.accountType?.let { AccountTypeResultSetMapper().apply(resultSet) }
            bank = model.bank?.let { BankResultSetMapper().apply(resultSet) }
        }
    }
}
