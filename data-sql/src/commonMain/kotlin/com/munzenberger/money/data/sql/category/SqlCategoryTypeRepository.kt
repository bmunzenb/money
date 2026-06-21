package com.munzenberger.money.data.sql.category

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.data.api.category.CategoryTypeRepository
import com.munzenberger.money.data.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

class SqlCategoryTypeRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : CategoryTypeRepository {

    override val categoryTypes: Flow<List<CategoryType>> = database.categoryTypeQueries
        .selectAll { id, value ->
            CategoryType(
                id = CategoryTypeId(id),
                value = CategoryTypeConstant.valueOf(value),
            )
        }
        .asFlow()
        .mapToList(context)
}
