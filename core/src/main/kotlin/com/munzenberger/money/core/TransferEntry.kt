package com.munzenberger.money.core

import com.munzenberger.money.core.model.TransferEntryModel
import com.munzenberger.money.core.model.TransferEntryTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

class TransferEntry internal constructor(model: TransferEntryModel) : Persistable<TransferEntryModel>(model, TransferEntryTable) {

    constructor() : this(TransferEntryModel(
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

    var orderInTransaction: Int?
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
                getAll(executor, TransferEntryTable, TransferEntryResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, TransferEntryTable, TransferEntryResultSetMapper())
    }
}

class TransferEntryResultSetMapper : ResultSetMapper<TransferEntry> {

    override fun apply(resultSet: ResultSet): TransferEntry {

        val model = TransferEntryModel().apply {
            TransferEntryTable.getValues(resultSet, this)
        }

        return TransferEntry(model).apply {
            account = model.account?.let { AccountResultSetMapper().apply(resultSet) }
        }
    }
}
