package com.munzenberger.money.sql

import java.lang.StringBuilder

fun insertQuery(into: String, block: InsertQueryBuilder.() -> Unit): Query {
    val insertQueryBuilder = InsertQueryBuilder(table = into)
    insertQueryBuilder.block()
    return insertQueryBuilder.build()
}

@InsertQueryMarker
class InsertQueryBuilder(table: String) : SettableQueryBuilder<InsertQueryBuilder>(table) {

    override fun instance() = this

    override fun build(table: String, parameters: Map<String, Any?>): Query {

        val sb = StringBuilder("INSERT INTO $table (")
        val params = mutableListOf<Any?>()

        sb.append(parameters.entries.joinToString(separator = ", ") {
            params.add(it.value)
            it.key
        })

        sb.append(") VALUES (")
        sb.append(parameters.entries.map { '?' }.joinToString(", "))
        sb.append(")")

        return Query(sb.toString(), params)
    }
}

@DslMarker
annotation class InsertQueryMarker
