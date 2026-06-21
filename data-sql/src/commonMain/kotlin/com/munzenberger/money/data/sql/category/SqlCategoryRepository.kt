package com.munzenberger.money.data.sql.category

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.category.CategoryRepository
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.data.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlCategoryRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : CategoryRepository {

    override val categories: Flow<List<Category>> = database.categoryQueries
        .selectAll { id, name, parentId, memo, typeId, typeValue ->
            Category(
                id = CategoryId(Uuid.parse(id)),
                name = name,
                parentId = parentId?.let { CategoryId(Uuid.parse(it)) },
                type = CategoryType(
                    id = CategoryTypeId(typeId),
                    value = CategoryTypeConstant.valueOf(typeValue),
                ),
                memo = memo,
            )
        }
        .asFlow()
        .mapToList(context)

    override suspend fun add(category: Category) {
        withContext(context) {
            database.categoryQueries.insert(
                id = category.id.value.toString(),
                name = category.name,
                parent_id = category.parentId?.value?.toString(),
                type_id = category.type.id.value,
                memo = category.memo,
            )
        }
    }

    override suspend fun update(category: Category) {
        withContext(context) {
            database.categoryQueries.update(
                name = category.name,
                parent_id = category.parentId?.value?.toString(),
                type_id = category.type.id.value,
                memo = category.memo,
                id = category.id.value.toString(),
            )
        }
    }

    override suspend fun removeById(categoryId: CategoryId) {
        withContext(context) {
            database.categoryQueries.deleteById(categoryId.value.toString())
        }
    }
}
