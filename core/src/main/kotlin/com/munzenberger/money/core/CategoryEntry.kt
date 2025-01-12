package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryEntryModel
import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.sql.OrderableQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet

data class CategoryEntryIdentity(override val value: Long) : EntryIdentity

class CategoryEntry internal constructor(model: CategoryEntryModel) :
    AbstractMoneyEntity<CategoryEntryIdentity, CategoryEntryModel>(
        model,
        CategoryEntryTable,
    ),
    Entry<CategoryEntryIdentity> {
        constructor() : this(
            CategoryEntryModel(
                orderInTransaction = 0,
            ),
        )

        override val identity: CategoryEntryIdentity?
            get() = model.identity?.let { CategoryEntryIdentity(it) }

        internal val transactionRef = PersistableIdentityReference(model.transaction?.let { TransactionIdentity(it) })

        override fun setTransaction(transaction: Transaction) {
            transactionRef.set(transaction)
        }

        var category: Category? = null

        override var amount: Money?
            get() = model.amount?.let { Money.valueOf(it) }
            set(value) {
                model.amount = value?.value
            }

        override var memo: String?
            get() = model.memo
            set(value) {
                model.memo = value
            }

        override var orderInTransaction: Int?
            get() = model.orderInTransaction
            set(value) {
                model.orderInTransaction = value
            }

        override fun save(executor: QueryExecutor) =
            executor.transaction { tx ->
                model.transaction = transactionRef.getIdentity(tx)?.value
                model.category = category.getIdentity(tx)?.value
                super.save(tx)
            }

        companion object {
            fun find(
                executor: QueryExecutor,
                block: OrderableQueryBuilder<*>.() -> Unit = {},
            ) = find(executor, CategoryEntryTable, CategoryEntryResultSetMapper, block)

            fun get(
                identity: CategoryEntryIdentity,
                executor: QueryExecutor,
            ) = get(identity, executor, CategoryEntryTable, CategoryEntryResultSetMapper)
        }
    }

object CategoryEntryResultSetMapper : ResultSetMapper<CategoryEntry> {
    override fun apply(resultSet: ResultSet): CategoryEntry {
        val model =
            CategoryEntryModel().apply {
                CategoryEntryTable.getValues(resultSet, this)
            }

        return CategoryEntry(model).apply {
            category = model.category?.let { CategoryResultSetMapper.apply(resultSet) }
        }
    }
}
