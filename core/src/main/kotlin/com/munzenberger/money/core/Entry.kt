package com.munzenberger.money.core

import com.munzenberger.money.core.model.EntryModel
import com.munzenberger.money.core.model.EntryTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

class Entry internal constructor(model: EntryModel) : Persistable<EntryModel>(model, EntryTable) {

    constructor() : this(EntryModel(
            orderInTransaction = 0
    ))

    internal val transactionRef = PersistableIdentityReference(model.transaction)

    fun setTransaction(transaction: Transaction) {
        transactionRef.set(transaction)
    }

    internal val categoryRef = PersistableIdentityReference(model.category)

    fun setCategory(category: Category) {
        categoryRef.set(category)
    }

    var amount: Money?
        get() = model.amount?.let { Money.valueOf(it) }
        set(value) { model.amount = value?.value }

    var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    var orderInTransaction: Int?
        get() = model.orderInTransaction
        set(value) { model.orderInTransaction = value }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
        model.transaction = transactionRef.getIdentity(tx)
        model.category = categoryRef.getIdentity(tx)
        super.save(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, EntryTable, EntryResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, EntryTable, EntryResultSetMapper())
    }
}

class EntryResultSetMapper : ResultSetMapper<Entry> {

    override fun apply(resultSet: ResultSet): Entry {

        val model = EntryModel().apply {
            EntryTable.getValues(resultSet, this)
        }

        return Entry(model)
    }
}
