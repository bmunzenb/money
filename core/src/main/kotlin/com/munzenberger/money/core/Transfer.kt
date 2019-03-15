package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransferModel
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLongOrNull
import io.reactivex.Completable
import java.sql.ResultSet

class Transfer(model: TransferModel = TransferModel()) : Persistable<TransferModel>(model, TransferTable) {

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

        val transactionIdentity = transaction.getIdentity(executor) { model.transaction = it }
        val categoryIdentity = Persistable.getIdentity(category, executor) { model.category = it }

        return completableChain(transactionIdentity, categoryIdentity, super.save(executor))
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, TransferTable, TransferResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, TransferTable, TransferResultSetMapper(), Transfer::class)
    }
}

class TransferResultSetMapper : ResultSetMapper<Transfer> {

    override fun map(resultSet: ResultSet): Transfer {

        val model = TransferModel().apply {
            identity = resultSet.getLong(TransferTable.identityColumn)
            transaction = resultSet.getLongOrNull(TransferTable.transactionColumn)
            category = resultSet.getLongOrNull(TransferTable.categoryColumn)
            amount = resultSet.getLong(TransferTable.amountColumn)
            memo = resultSet.getString(TransferTable.memoColumn)
        }

        return Transfer(model).apply {
            category = model.category?.let { CategoryResultSetMapper().map(resultSet) }
        }
    }
}
