package com.munzenberger.money.data.api.category

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    val categories: Flow<List<Category>>

    suspend fun add(category: Category)

    suspend fun update(category: Category)

    suspend fun removeById(categoryId: CategoryId)
}

suspend fun CategoryRepository.remove(category: Category) {
    removeById(category.id)
}
