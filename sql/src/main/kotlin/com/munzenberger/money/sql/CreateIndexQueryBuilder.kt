package com.munzenberger.money.sql

import java.lang.StringBuilder

class CreateIndexQueryBuilder(
        private val name: String,
        private val table: String,
) {
    private var unique: Boolean = false
    private val columns = mutableListOf<String>()

    fun unique() = this.apply {
        unique = true
    }

    fun column(name: String) = this.apply {
        columns.add(name)
    }

    fun build(): Query {

        val sql = StringBuilder("CREATE ").apply {

            if (unique) {
                append("UNIQUE ")
            }

            append("INDEX ")
            append(name)
            append(" ON ")
            append(table)
            append(" (")
            append(columns.joinToString(", "))
            append(")")
        }

        return Query(sql.toString())
    }
}
