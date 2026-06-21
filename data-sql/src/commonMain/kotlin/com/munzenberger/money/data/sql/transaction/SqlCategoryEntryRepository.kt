package com.munzenberger.money.data.sql.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.CategoryEntryId
import com.munzenberger.money.data.api.transaction.CategoryEntryRepository
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlCategoryEntryRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : CategoryEntryRepository {

    override suspend fun categoryEntriesByTransactionId(transactionId: TransactionId): Flow<List<CategoryEntry>> =
        database.categoryEntryQueries
            .selectByTransactionId(transactionId.value.toString(), ::mapCategoryEntry)
            .asFlow()
            .mapToList(context)

    override suspend fun categoryEntriesByCategoryId(categoryId: CategoryId): Flow<List<CategoryEntry>> =
        database.categoryEntryQueries
            .selectByCategoryId(categoryId.value.toString(), ::mapCategoryEntry)
            .asFlow()
            .mapToList(context)

    override suspend fun add(categoryEntry: CategoryEntry) {
        withContext(context) {
            database.categoryEntryQueries.insert(
                id = categoryEntry.id.value.toString(),
                transaction_id = categoryEntry.transactionId.value.toString(),
                category_id = categoryEntry.categoryId.value.toString(),
                amount = categoryEntry.amount.value,
                memo = categoryEntry.memo,
                order_in_transaction = categoryEntry.orderInTransaction.toLong(),
            )
        }
    }

    override suspend fun update(categoryEntry: CategoryEntry) {
        withContext(context) {
            database.categoryEntryQueries.update(
                transaction_id = categoryEntry.transactionId.value.toString(),
                category_id = categoryEntry.categoryId.value.toString(),
                amount = categoryEntry.amount.value,
                memo = categoryEntry.memo,
                order_in_transaction = categoryEntry.orderInTransaction.toLong(),
                id = categoryEntry.id.value.toString(),
            )
        }
    }

    override suspend fun removeById(categoryEntryId: CategoryEntryId) {
        withContext(context) {
            database.categoryEntryQueries.deleteById(categoryEntryId.value.toString())
        }
    }

    private fun mapCategoryEntry(
        id: String,
        transactionId: String,
        categoryId: String,
        amount: Long,
        memo: String?,
        orderInTransaction: Long,
    ) = CategoryEntry(
        id = CategoryEntryId(Uuid.parse(id)),
        transactionId = TransactionId(Uuid.parse(transactionId)),
        categoryId = CategoryId(Uuid.parse(categoryId)),
        amount = Money(amount),
        memo = memo,
        orderInTransaction = orderInTransaction.toInt(),
    )
}
