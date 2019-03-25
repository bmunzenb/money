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

    private val transaction = PersistableIdentityReference()

    fun setTransaction(transaction: Transaction) {
        this.transaction.set(transaction)
    }

    override fun save(executor: QueryExecutor): Completable {

        val tx = executor.createTransaction()

        val transactionIdentity = transaction.getIdentity(tx) { model.transaction = it }
        val categoryIdentity = Persistable.getIdentity(category, tx) { model.category = it }

        return concatAll(transactionIdentity, categoryIdentity, super.save(tx)).withTransaction(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, TransferTable, TransferResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, TransferTable, TransferResultSetMapper(), Transfer::class)
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
