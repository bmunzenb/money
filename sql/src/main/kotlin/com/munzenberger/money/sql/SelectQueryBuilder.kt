package com.munzenberger.money.sql

import java.lang.StringBuilder

class SelectQueryBuilder(private val table: String) {

    private val columns = mutableListOf<String>()
    private var where: Condition? = null
    private val joins = mutableListOf<String>()
    private val orderBy = mutableListOf<String>()

    fun cols(columns: List<String>) = this.apply {
        this.columns.addAll(columns)
    }

    fun cols(vararg columns: String) = this.apply {
        this.columns.addAll(columns)
    }

    fun where(condition: Condition) = this.apply {
        this.where = condition
    }

    fun innerJoin(leftTable: String, leftColumn: String, rightTable: String, rightColumn: String) = this.apply {
        joins.add("INNER JOIN $rightTable ON $leftTable.$leftColumn = $rightTable.$rightColumn")
    }

    fun leftJoin(leftTable: String, leftColumn: String, rightTable: String, rightColumn: String) = this.apply {
        joins.add("LEFT JOIN $rightTable ON $leftTable.$leftColumn = $rightTable.$rightColumn")
    }

    fun rightJoin(leftTable: String, leftColumn: String, rightTable: String, rightColumn: String) = this.apply {
        joins.add("RIGHT JOIN $rightTable ON $leftTable.$leftColumn = $rightTable.$rightColumn")
    }

    fun orderBy(vararg columns: String) = this.apply {
        columns.forEach { orderBy(it) }
    }

    fun orderBy(column: String, descending: Boolean = false) = this.apply {
        val dir = if (descending) "DESC" else "ASC"
        orderBy.add("$column $dir")
    }

    fun build(): Query {

        val sb = StringBuilder("SELECT ")
        val params = mutableListOf<Any?>()

        val selectColumns =
                if (columns.isEmpty()) "*"
                else columns.joinToString(", ")

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

        return Query(sb.toString(), params)
    }
}
