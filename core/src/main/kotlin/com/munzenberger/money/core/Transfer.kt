package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransferModel
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet

class Transfer(executor: QueryExecutor, model: TransferModel = TransferModel()) : Persistable<TransferModel>(model, TransferTable, executor) {

    var amount: Long?
        get() = model.amount
        set(value) { model.amount = value }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var category: Category? = null

    private var transaction: Transaction? = null
    private var transactionUpdated = false

    fun setTransaction(transaction: Transaction) {
        this.transaction = transaction
        this.transactionUpdated = true
    }

    override fun save(): Completable {

        val transactionIdentity = when {
            this.transactionUpdated -> Persistable.getIdentity(transaction) { model.transaction = it }
            else -> Completable.complete()
        }

        val categoryIdentity = Persistable.getIdentity(category) { model.category = it }

        return transactionIdentity.andThen(categoryIdentity).andThen(super.save())
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, TransferTable, TransferResultSetMapper(executor))

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, TransferTable, TransferResultSetMapper(executor), Transfer::class)
    }
}

class TransferResultSetMapper(private val executor: QueryExecutor) : ResultSetMapper<Transfer> {

    override fun map(resultSet: ResultSet): Transfer {

        val model = TransferModel().apply {
            identity = resultSet.getLong(TransferTable.identityColumn)
            transaction = resultSet.getLongOrNull(TransferTable.transactionColumn)
            category = resultSet.getLongOrNull(TransferTable.categoryColumn)
            amount = resultSet.getLong(TransferTable.amountColumn)
            memo = resultSet.getString(TransferTable.memoColumn)
        }

        return Transfer(executor, model).apply {
            category = model.category?.let { CategoryResultSetMapper(executor).map(resultSet) }
        }
    }
}
