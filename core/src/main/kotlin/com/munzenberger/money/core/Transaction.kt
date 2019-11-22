package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionModel
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.doInTransaction
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import io.reactivex.Single
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

    override fun save(executor: QueryExecutor) = executor.doInTransaction { tx ->
        model.account = account.getIdentity(tx)
        model.payee = payee.getIdentity(tx)
        super.save(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, TransactionTable, TransactionResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, TransactionTable, TransactionResultSetMapper())
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

fun Transaction.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Transaction> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Transaction::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Transaction.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }
