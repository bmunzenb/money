package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransactionModel
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet
import java.time.LocalDate

class Transaction internal constructor(model: TransactionModel) : Persistable<TransactionModel>(model, TransactionTable) {

    constructor() : this(TransactionModel(
            status = TransactionStatus.UNRECONCILED
    ))

    var date: LocalDate?
        get() = model.date?.let { LocalDate.ofEpochDay(it) }
        set(value) { model.date = value?.toEpochDay() }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var number: String?
        get() = model.number
        set(value) { model.number = value }

    var account: Account? = null

    var payee: Payee? = null

    var status: TransactionStatus?
        get() = model.status
        set(value) { model.status = value }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
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
            TransactionTable.getValues(resultSet, this)
        }

        return Transaction(model).apply {
            account = model.account?.let { AccountResultSetMapper().apply(resultSet) }
            payee = model.payee?.let { PayeeResultSetMapper().apply(resultSet) }
        }
    }
}
