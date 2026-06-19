package com.munzenberger.money.repository.sql.category

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.munzenberger.money.repository.api.category.Category
import com.munzenberger.money.repository.api.category.CategoryId
import com.munzenberger.money.repository.api.category.CategoryRepository
import com.munzenberger.money.repository.api.category.CategoryType
import com.munzenberger.money.repository.api.category.CategoryTypeConstant
import com.munzenberger.money.repository.api.category.CategoryTypeId
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SqlCategoryRepository(
    driver: SqlDriver,
    private val context: CoroutineContext = Dispatchers.IO,
) : CategoryRepository {

    private val database = MoneyDatabase(driver)

    override val categories: Flow<List<Category>> = database.categoryQueries
        .selectAll { id, name, parentId, memo, typeId, typeValue ->
            Category(
                id = CategoryId(Uuid.parse(id)),
                name = name,
                parent = parentId?.let { CategoryId(Uuid.parse(it)) },
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
                parent_id = category.parent?.value?.toString(),
                type_id = category.type.id.value,
                memo = category.memo,
            )
        }
    }

    override suspend fun update(category: Category) {
        withContext(context) {
            database.categoryQueries.update(
                name = category.name,
                parent_id = category.parent?.value?.toString(),
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
