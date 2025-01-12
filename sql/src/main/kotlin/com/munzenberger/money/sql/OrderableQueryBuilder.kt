package com.munzenberger.money.sql

abstract class OrderableQueryBuilder<T> : ConditionalQueryBuilder<T>() {
    private val orderBy = mutableListOf<String>()

    fun orderBy(vararg columns: String): T {
        columns.forEach { orderBy(it) }
        return instance()
    }

    fun orderBy(
        column: String,
        descending: Boolean = false,
    ): T {
        val dir = if (descending) "DESC" else "ASC"
        orderBy.add("$column $dir")
        return instance()
    }

    override fun build(where: Condition?) = build(where, orderBy)

    abstract fun build(
        where: Condition?,
        orderBy: List<String>,
    ): Query
}
