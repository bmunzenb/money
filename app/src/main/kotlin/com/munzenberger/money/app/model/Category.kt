package com.munzenberger.money.app.model

import com.munzenberger.money.core.Category
import com.munzenberger.money.core.CategoryResultSetMapper
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

fun Category.Companion.getAllWithParent(database: MoneyDatabase): List<Pair<Category, String?>> {

    val sql = """
        SELECT CATEGORIES.*, PARENTS.CATEGORY_NAME AS PARENT_NAME
        FROM CATEGORIES
        LEFT JOIN CATEGORIES AS PARENTS ON CATEGORIES.CATEGORY_PARENT_ID = PARENTS.CATEGORY_ID
    """.trimIndent()

    return database.getList(Query(sql), object : ResultSetMapper<Pair<Category, String?>> {

        private val categoryMapper = CategoryResultSetMapper()

        override fun apply(rs: ResultSet): Pair<Category, String?> {

            val category = categoryMapper.apply(rs)
            val parentName = rs.getString("PARENT_NAME")

            return category to parentName
        }
    })
}
