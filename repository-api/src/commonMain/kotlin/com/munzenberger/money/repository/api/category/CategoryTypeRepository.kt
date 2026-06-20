package com.munzenberger.money.repository.api.category

import kotlinx.coroutines.flow.Flow

interface CategoryTypeRepository {
    val categoryTypes: Flow<List<CategoryType>>
}
