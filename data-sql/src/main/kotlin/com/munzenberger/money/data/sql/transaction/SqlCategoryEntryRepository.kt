package com.munzenberger.money.data.sql.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.CategoryEntryId
import com.munzenberger.money.data.api.transaction.CategoryEntryRepository
import com.munzenberger.money.data.api.transaction.CategoryEntryWriter
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.requireOneRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlCategoryEntryRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : CategoryEntryRepository, CategoryEntryWriter {

    override suspend fun categoryEntriesByTransactionId(transactionId: TransactionId): Flow<List<CategoryEntry>> =
        database.categoryEntryQueries
            .selectByTransactionId(transactionId.value, ::mapCategoryEntry)
            .asFlow()
            .mapToList(context)

    override suspend fun categoryEntriesByCategoryId(categoryId: CategoryId): Flow<List<CategoryEntry>> =
        database.categoryEntryQueries
            .selectByCategoryId(categoryId.value, ::mapCategoryEntry)
            .asFlow()
            .mapToList(context)

    override fun add(categoryEntry: CategoryEntry) {
        database.categoryEntryQueries.insert(
            id = categoryEntry.id.value,
            transaction_id = categoryEntry.transactionId.value,
            category_id = categoryEntry.categoryId.value,
            amount = categoryEntry.amount.value,
            memo = categoryEntry.memo,
            order_in_transaction = categoryEntry.orderInTransaction.toLong(),
        )
    }

    override fun update(categoryEntry: CategoryEntry) {
        database.categoryEntryQueries.update(
            transaction_id = categoryEntry.transactionId.value,
            category_id = categoryEntry.categoryId.value,
            amount = categoryEntry.amount.value,
            memo = categoryEntry.memo,
            order_in_transaction = categoryEntry.orderInTransaction.toLong(),
            id = categoryEntry.id.value,
        ).requireOneRow { "No category entry with id ${categoryEntry.id.value}" }
    }

    override fun removeById(categoryEntryId: CategoryEntryId) {
        database.categoryEntryQueries.deleteById(categoryEntryId.value)
    }

    private fun mapCategoryEntry(
        id: Uuid,
        transactionId: Uuid,
        categoryId: Uuid,
        amount: Long,
        memo: String?,
        orderInTransaction: Long,
    ) = CategoryEntry(
        id = CategoryEntryId(id),
        transactionId = TransactionId(transactionId),
        categoryId = CategoryId(categoryId),
        amount = Money(amount),
        memo = memo,
        orderInTransaction = orderInTransaction.toInt(),
    )
}
