package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransferModel
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

class Transfer internal constructor(model: TransferModel) : Persistable<TransferModel>(model, TransferTable) {

    constructor() : this(TransferModel(
            status = TransactionStatus.UNRECONCILED,
            orderInTransaction = 0
    ))

    var amount: Money?
        get() = model.amount?.let { Money.valueOf(it) }
        set(value) { model.amount = value?.value }

    var number: String?
        get() = model.number
        set(value) { model.number = value }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var account: Account? = null

    var status: TransactionStatus?
        get() = model.status
        set(value) { model.status = value }

    var orderInTransaction: Long?
        get() = model.orderInTransaction
        set(value) { model.orderInTransaction = value }

    internal val transactionRef = PersistableIdentityReference(model.transaction)

    fun setTransaction(transaction: Transaction) {
        this.transactionRef.set(transaction)
    }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
        model.transaction = transactionRef.getIdentity(tx)
        model.account = account.getIdentity(tx)
        super.save(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, TransferTable, TransferResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, TransferTable, TransferResultSetMapper())
    }
}

class TransferResultSetMapper : ResultSetMapper<Transfer> {

    override fun apply(resultSet: ResultSet): Transfer {

        val model = TransferModel().apply {
            TransferTable.getValues(resultSet, this)
        }

        return Transfer(model).apply {
            account = model.account?.let { AccountResultSetMapper().apply(resultSet) }
        }
    }
}
