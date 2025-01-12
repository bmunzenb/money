package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import com.munzenberger.money.core.CategoryIdentity
import com.munzenberger.money.core.CategoryResultSetMapper
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.core.model.CategoryType
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.getLongOrNull
import com.munzenberger.money.sql.isNotNull
import com.munzenberger.money.sql.isNull
import java.sql.ResultSet

data class CategoryWithParent(
    val category: Category,
    val parentId: Long?,
    val parentName: String?,
) {
    val identity: CategoryIdentity
        get() = category.identity!!

    val name: String?
        get() = category.name

    val type: CategoryType?
        get() = category.type
}

fun Category.Companion.getAllWithParent(database: MoneyDatabase): List<CategoryWithParent> {
    val sql =
        """
        SELECT
            ${CategoryTable.tableName}.*, 
            PARENTS.${CategoryTable.identityColumn} AS PARENT_ID, 
            PARENTS.${CategoryTable.CATEGORY_NAME} AS PARENT_NAME
        FROM ${CategoryTable.tableName}
        LEFT JOIN ${CategoryTable.tableName} AS PARENTS ON ${CategoryTable.tableName}.${CategoryTable.CATEGORY_PARENT_ID} = PARENTS.${CategoryTable.identityColumn}
        """.trimIndent()

    return database.getList(
        Query(sql),
        object : ResultSetMapper<CategoryWithParent> {
            private val categoryMapper = CategoryResultSetMapper()

            override fun apply(rs: ResultSet): CategoryWithParent {
                val category = categoryMapper.apply(rs)

                val parentId = rs.getLongOrNull("PARENT_ID")
                val parentName = rs.getString("PARENT_NAME")

                return CategoryWithParent(category, parentId, parentName)
            }
        },
    )
}

fun Category.Companion.find(
    executor: QueryExecutor,
    name: String? = null,
    isParent: Boolean? = null,
    parentId: CategoryIdentity? = null,
): List<Category> {
    var condition =
        name?.let {
            CategoryTable.CATEGORY_NAME.eq(name)
        }

    isParent?.let {
        val c =
            when (it) {
                true -> CategoryTable.CATEGORY_PARENT_ID.isNull()
                else -> CategoryTable.CATEGORY_PARENT_ID.isNotNull()
            }

        condition = c and condition
    }

    parentId?.let {
        condition = CategoryTable.CATEGORY_PARENT_ID.eq(it.value) and condition
    }

    val query =
        CategoryTable.select {
            condition?.let {
                where(it)
            }
        }

    return executor.getList(query, CategoryResultSetMapper())
}
