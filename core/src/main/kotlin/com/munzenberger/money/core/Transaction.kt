package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionModel
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet
import java.util.*

class Transaction internal constructor(model: TransactionModel) : Persistable<TransactionModel>(model, TransactionTable) {

    constructor() : this(TransactionModel())

    var date: Date?
        get() = model.date?.let { Date(it) }
        set(value) { model.date = value?.time }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var account: Account? = null

    var payee: Payee? = null

    override fun save(executor: QueryExecutor): Completable {

        val tx = executor.createTransaction()

        val accountIdentity = Persistable.getIdentity(account, tx) { model.account = it }
        val payeeIdentity = Persistable.getIdentity(payee, tx) { model.payee = it }

        return concatAll(accountIdentity, payeeIdentity, super.save(tx)).withTransaction(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, TransactionTable, TransactionResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, TransactionTable, TransactionResultSetMapper(), Transaction::class)
    }
}

class TransactionResultSetMapper : ResultSetMapper<Transaction> {

    override fun apply(resultSet: ResultSet): Transaction {

        val model = TransactionModel().apply {
            identity = resultSet.getLong(TransactionTable.identityColumn)
            account = resultSet.getLongOrNull(TransactionTable.accountColumn)
            payee = resultSet.getLongOrNull(TransactionTable.payeeColumn)
            date = resultSet.getLongOrNull(TransactionTable.dateColumn)
            memo = resultSet.getString(TransactionTable.memoColumn)
        }

        return Transaction(model).apply {
            account = model.account?.let { AccountResultSetMapper().apply(resultSet) }
            payee = model.payee?.let { PayeeResultSetMapper().apply(resultSet) }
        }
    }
}
