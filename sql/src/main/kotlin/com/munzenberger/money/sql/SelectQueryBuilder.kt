package com.munzenberger.money.sql

import java.lang.StringBuilder

fun selectQuery(
    from: String,
    block: SelectQueryBuilder.() -> Unit,
): Query {
    val selectQueryBuilder = SelectQueryBuilder(table = from)
    selectQueryBuilder.block()
    return selectQueryBuilder.build()
}

@SelectQueryMarker
class SelectQueryBuilder(
    private val table: String,
) : OrderableQueryBuilder<SelectQueryBuilder>() {
    private val columns = mutableListOf<String>()
    private val joins = mutableListOf<String>()
    private val groupBy = mutableListOf<String>()

    fun cols(columns: List<String>) =
        this.apply {
            this.columns.addAll(columns)
        }

    fun cols(vararg columns: String) =
        this.apply {
            this.columns.addAll(columns)
        }

    fun innerJoin(
        leftTable: String,
        leftColumn: String,
        rightTable: String,
        rightColumn: String,
    ) = this.apply {
        joins.add("INNER JOIN $rightTable ON $leftTable.$leftColumn = $rightTable.$rightColumn")
    }

    fun leftJoin(
        leftTable: String,
        leftColumn: String,
        rightTable: String,
        rightColumn: String,
    ) = this.apply {
        joins.add("LEFT JOIN $rightTable ON $leftTable.$leftColumn = $rightTable.$rightColumn")
    }

    fun rightJoin(
        leftTable: String,
        leftColumn: String,
        rightTable: String,
        rightColumn: String,
    ) = this.apply {
        joins.add("RIGHT JOIN $rightTable ON $leftTable.$leftColumn = $rightTable.$rightColumn")
    }

    fun groupBy(vararg columns: String) =
        this.apply {
            groupBy.clear()
            groupBy.addAll(columns)
        }

    override fun build(
        where: Condition?,
        orderBy: List<String>,
    ): Query {
        val sb = StringBuilder("SELECT ")
        val params = mutableListOf<Any?>()

        val selectColumns =
            if (columns.isEmpty()) {
                "*"
            } else {
                columns.joinToString(", ")
            }

        sb.append(selectColumns)
        sb.append(" FROM $table")

        joins.forEach { sb.append(" $it") }

        where?.run {
            sb.append(" WHERE $clause")
            params.addAll(parameters)
        }

        if (orderBy.isNotEmpty()) {
            sb.append(" ORDER BY ")
            sb.append(orderBy.joinToString(", "))
        }

        if (groupBy.isNotEmpty()) {
            sb.append(" GROUP BY ")
            sb.append(groupBy.joinToString(", "))
        }

        return Query(sb.toString(), params)
    }

    override fun instance() = this
}

@DslMarker
annotation class SelectQueryMarker
