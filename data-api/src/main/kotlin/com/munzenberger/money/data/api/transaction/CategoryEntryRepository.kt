package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.category.CategoryId
import kotlinx.coroutines.flow.Flow

interface CategoryEntryRepository {
    suspend fun categoryEntriesByTransactionId(transactionId: TransactionId): Flow<List<CategoryEntry>>

    suspend fun categoryEntriesByCategoryId(categoryId: CategoryId): Flow<List<CategoryEntry>>
}
