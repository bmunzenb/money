package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionModel
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet
import java.util.*

class Transaction(executor: QueryExecutor, model: TransactionModel = TransactionModel()) : Persistable<TransactionModel>(model, TransactionTable, executor) {

    var date: Date?
        get() = model.date?.let { Date(it) }
        set(value) { model.date = value?.time }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var account: Account? = null

    var payee: Payee? = null

    override fun save(): Completable {

        val accountIdentity = Persistable.getIdentity(account) { model.account = it }
        val payeeIdentity = Persistable.getIdentity(payee) { model.payee = it }

        return accountIdentity.andThen(payeeIdentity).andThen(super.save())
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, TransactionTable, TransactionResultSetMapper(executor))

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, TransactionTable, TransactionResultSetMapper(executor), Transaction::class)
    }
}

class TransactionResultSetMapper(private val executor: QueryExecutor) : ResultSetMapper<Transaction> {

    override fun map(resultSet: ResultSet): Transaction {

        val model = TransactionModel().apply {
            identity = resultSet.getLong(TransactionTable.identityColumn)
            account = resultSet.getLongOrNull(TransactionTable.accountColumn)
            payee = resultSet.getLongOrNull(TransactionTable.payeeColumn)
            date = resultSet.getLongOrNull(TransactionTable.dateColumn)
            memo = resultSet.getString(TransactionTable.memoColumn)
        }

        return Transaction(executor, model).apply {
            account = model.account?.let { AccountResultSetMapper(executor).map(resultSet) }
            payee = model.payee?.let { PayeeResultSetMapper(executor).map(resultSet) }
        }
    }
}
