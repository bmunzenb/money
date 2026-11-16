package com.munzenberger.money.core.category

import com.munzenberger.money.core.asResult
import com.munzenberger.money.data.api.MoneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Returns a flow of the repository's categories, each with its parent category, wrapped in a [Result] (see
 * [asResult]).
 *
 * The parents are looked up among the categories read, so the categories are read once. A category with no parent, or
 * whose parent isn't in the repository, has a null parent. The categories are in the repository's order.
 */
class GetCategoriesUseCase {
    operator fun invoke(repository: MoneyRepository): Flow<Result<List<CategoryWithParent>>> =
        repository.categories.map { categories ->
            val categoriesById = categories.associateBy { it.id }
            categories.map { category ->
                CategoryWithParent(category = category, parent = category.parentId?.let { categoriesById[it] })
            }
        }.asResult()
}
