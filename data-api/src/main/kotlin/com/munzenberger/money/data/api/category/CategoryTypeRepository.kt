package com.munzenberger.money.data.api.category

import kotlinx.coroutines.flow.Flow

interface CategoryTypeRepository {
    val categoryTypes: Flow<List<CategoryType>>
}
