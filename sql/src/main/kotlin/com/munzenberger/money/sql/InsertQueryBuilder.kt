package com.munzenberger.money.sql

import java.lang.StringBuilder

// TODO create a DSL for this builder
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
