package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionModel
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet
import java.util.*

class Transaction(model: TransactionModel = TransactionModel()) : Persistable<TransactionModel>(model, TransactionTable) {

    var date: Date?
        get() = model.date?.let { Date(it) }
        set(value) { model.date = value?.time }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var account: Account? = null

    var payee: Payee? = null

    override fun save(executor: QueryExecutor): Completable {

        val accountIdentity = Persistable.getIdentity(account, executor) { model.account = it }
        val payeeIdentity = Persistable.getIdentity(payee, executor) { model.payee = it }

        return completableChain(accountIdentity, payeeIdentity, super.save(executor))
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, TransactionTable, TransactionResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, TransactionTable, TransactionResultSetMapper(), Transaction::class)
    }
}

class TransactionResultSetMapper : ResultSetMapper<Transaction> {

    override fun map(resultSet: ResultSet): Transaction {

        val model = TransactionModel().apply {
            identity = resultSet.getLong(TransactionTable.identityColumn)
            account = resultSet.getLongOrNull(TransactionTable.accountColumn)
            payee = resultSet.getLongOrNull(TransactionTable.payeeColumn)
            date = resultSet.getLongOrNull(TransactionTable.dateColumn)
            memo = resultSet.getString(TransactionTable.memoColumn)
        }

        return Transaction(model).apply {
            account = model.account?.let { AccountResultSetMapper().map(resultSet) }
            payee = model.payee?.let { PayeeResultSetMapper().map(resultSet) }
        }
    }
}
