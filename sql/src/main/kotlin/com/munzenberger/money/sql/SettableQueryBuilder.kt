package com.munzenberger.money.sql

abstract class SettableQueryBuilder<T>(private val table: String) {
    private val parameters = mutableMapOf<String, Any?>()

    fun set(
        column: String,
        value: Any?,
    ): T {
        parameters[column] = value
        return instance()
    }

    fun build() = build(table, parameters)

    // TODO: can this be removed now that the DSL should be used?
    protected abstract fun instance(): T

    protected abstract fun build(
        table: String,
        parameters: Map<String, Any?>,
    ): Query
}
