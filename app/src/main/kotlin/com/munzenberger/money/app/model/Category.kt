package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
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
        val parentName: String?
) {
    val identity: Long
        get() = category.identity!!

    val name: String?
        get() = category.name

    val type: CategoryType?
        get() = category.type
}

fun Category.Companion.getAllWithParent(database: MoneyDatabase): List<CategoryWithParent> {

    val sql = """
        SELECT
            ${CategoryTable.tableName}.*, 
            PARENTS.${CategoryTable.identityColumn} AS PARENT_ID, 
            PARENTS.${CategoryTable.nameColumn} AS PARENT_NAME
        FROM ${CategoryTable.tableName}
        LEFT JOIN ${CategoryTable.tableName} AS PARENTS ON ${CategoryTable.tableName}.${CategoryTable.parentColumn} = PARENTS.${CategoryTable.identityColumn}
    """.trimIndent()

    return database.getList(Query(sql), object : ResultSetMapper<CategoryWithParent> {

        private val categoryMapper = CategoryResultSetMapper()

        override fun apply(rs: ResultSet): CategoryWithParent {

            val category = categoryMapper.apply(rs)

            val parentId = rs.getLongOrNull("PARENT_ID")
            val parentName = rs.getString("PARENT_NAME")

            return CategoryWithParent(category, parentId, parentName)
        }
    })
}

fun Category.Companion.find(
        executor: QueryExecutor,
        name: String? = null,
        isParent: Boolean? = null,
        parentId: Long? = null
): List<Category> {

    val query = CategoryTable.select()

    var condition = name?.let {
        CategoryTable.nameColumn.eq(name)
    }

    isParent?.let {
        val c = when (it) {
            true -> CategoryTable.parentColumn.isNull()
            else -> CategoryTable.parentColumn.isNotNull()
        }

        condition = c and condition
    }

    parentId?.let {
        condition = CategoryTable.parentColumn.eq(it) and condition
    }

    condition?.let {
        query.where(it)
    }

    return executor.getList(query.build(), CategoryResultSetMapper())
}
