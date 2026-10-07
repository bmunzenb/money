package com.munzenberger.money.data.api.category

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    val categories: Flow<List<Category>>
}
