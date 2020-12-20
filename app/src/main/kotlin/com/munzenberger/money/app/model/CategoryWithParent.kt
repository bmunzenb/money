package com.munzenberger.money.app.model

import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

data class CategoryWithParent(
        val identity: Long,
        val name: String,
        val parentName: String?
) {
    companion object {

        fun getAll(executor: QueryExecutor): List<CategoryWithParent> {
            val mapper = CategoryWithParentResultSetMapper()
            return executor.getList(mapper.query, mapper)
        }
    }
}

private class CategoryWithParentResultSetMapper : ResultSetMapper<CategoryWithParent> {

    private val sql = """
        SELECT CATEGORIES.CATEGORY_ID, CATEGORIES.CATEGORY_NAME, PARENTS.CATEGORY_NAME AS PARENT_NAME
        FROM CATEGORIES
        LEFT JOIN CATEGORIES AS PARENTS ON CATEGORIES.CATEGORY_PARENT_ID = PARENTS.CATEGORY_ID
    """.trimIndent()

    val query = Query(sql)

    override fun apply(resultSet: ResultSet) =
            CategoryWithParent(
                    identity = resultSet.getLong("CATEGORY_ID"),
                    name = resultSet.getString("CATEGORY_NAME"),
                    parentName = resultSet.getString("PARENT_NAME")
            )
}
