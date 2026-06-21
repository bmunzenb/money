package com.munzenberger.money.repository.api.transaction

import com.munzenberger.money.repository.api.category.CategoryId
import kotlinx.coroutines.flow.Flow

interface CategoryEntryRepository {
    suspend fun categoryEntriesByTransactionId(transactionId: TransactionId): Flow<List<CategoryEntry>>

    suspend fun categoryEntriesByCategoryId(categoryId: CategoryId): Flow<List<CategoryEntry>>

    suspend fun add(categoryEntry: CategoryEntry)

    suspend fun update(categoryEntry: CategoryEntry)

    suspend fun removeById(categoryEntryId: CategoryEntryId)
}

suspend fun CategoryEntryRepository.remove(categoryEntry: CategoryEntry) {
    removeById(categoryEntry.id)
}
