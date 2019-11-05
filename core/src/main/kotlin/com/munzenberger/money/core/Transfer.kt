package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransferModel
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet

class Transfer internal constructor(model: TransferModel) : Persistable<TransferModel>(model, TransferTable) {

    constructor() : this(TransferModel())

    var amount: Long?
        get() = model.amount
        set(value) { model.amount = value }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var category: Category? = null

    private val transactionRef = PersistableIdentityReference()

    fun setTransaction(transaction: Transaction) {
        this.transactionRef.set(transaction)
    }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->

        val getTransactionIdentity = transactionRef.getIdentity(tx) { model.transaction = it }
        val getCategoryIdentity = getIdentity(category, tx) { model.category = it }

        Completable.concatArray(getTransactionIdentity, getCategoryIdentity, super.save(tx))
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, TransferTable, TransferResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, TransferTable, TransferResultSetMapper(), Transfer::class)
    }
}

class TransferResultSetMapper : ResultSetMapper<Transfer> {

    override fun apply(resultSet: ResultSet): Transfer {

        val model = TransferModel().apply {
            identity = resultSet.getLong(TransferTable.identityColumn)
            transaction = resultSet.getLongOrNull(TransferTable.transactionColumn)
            category = resultSet.getLongOrNull(TransferTable.categoryColumn)
            amount = resultSet.getLong(TransferTable.amountColumn)
            memo = resultSet.getString(TransferTable.memoColumn)
        }

        return Transfer(model).apply {
            category = model.category?.let { CategoryResultSetMapper().apply(resultSet) }
        }
    }
}
