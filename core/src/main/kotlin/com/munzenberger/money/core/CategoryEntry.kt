 package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryEntryModel
import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

class CategoryEntry internal constructor(model: CategoryEntryModel) : AbstractPersistable<CategoryEntryModel>(model, CategoryEntryTable), Entry {

    constructor() : this(CategoryEntryModel(
            orderInTransaction = 0
    ))

    internal val transactionRef = PersistableIdentityReference(model.transaction)

    override fun setTransaction(transaction: Transaction) {
        transactionRef.set(transaction)
    }

    var category: Category? = null

    override var amount: Money?
        get() = model.amount?.let { Money.valueOf(it) }
        set(value) { model.amount = value?.value }

    override var memo: String?
        get() = model.memo
        set(value) { model.memo = value }

    override var orderInTransaction: Int?
        get() = model.orderInTransaction
        set(value) { model.orderInTransaction = value }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
        model.transaction = transactionRef.getIdentity(tx)
        model.category = category.getIdentity(tx)
        super.save(tx)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, CategoryEntryTable, CategoryEntryResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, CategoryEntryTable, CategoryEntryResultSetMapper())
    }
}

class CategoryEntryResultSetMapper : ResultSetMapper<CategoryEntry> {

    override fun apply(resultSet: ResultSet): CategoryEntry {

        val model = CategoryEntryModel().apply {
            CategoryEntryTable.getValues(resultSet, this)
        }

        return CategoryEntry(model).apply {
            category = model.category?.let { CategoryResultSetMapper().apply(resultSet) }
        }
    }
}
