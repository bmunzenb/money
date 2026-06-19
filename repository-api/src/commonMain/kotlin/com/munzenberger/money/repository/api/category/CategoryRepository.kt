package com.munzenberger.money.repository.api.category

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface CategoryRepository {
    val categories: Flow<List<Category>>

    suspend fun add(category: Category)

    suspend fun update(category: Category)

    suspend fun removeById(categoryId: CategoryId)
}

@OptIn(ExperimentalUuidApi::class)
suspend fun CategoryRepository.remove(category: Category) {
    removeById(category.id)
}
