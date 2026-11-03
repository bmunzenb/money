package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.category.CategoryId
import kotlinx.coroutines.flow.Flow

interface CategoryEntryRepository {
    fun categoryEntriesByTransactionId(transactionId: TransactionId): Flow<List<CategoryEntry>>

    fun categoryEntriesByCategoryId(categoryId: CategoryId): Flow<List<CategoryEntry>>
}
