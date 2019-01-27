package com.munzenberger.money.sql

import java.lang.StringBuilder

class CreateTableQueryBuilder(private val table: String) {

    private var ifNotExists = false
    private val columns = mutableListOf<String>()
    private val constraints = mutableListOf<String>()

    fun ifNotExists() = this.apply {
        ifNotExists = true
    }

    fun column(name: String, type: String) = this.apply {
        columns.add("$name $type")
    }

    fun columnWithReference(name: String, type: String, refTable: String, refColumn: String) = this.apply {
        columns.add("$name $type REFERENCES $refTable ($refColumn)")
    }

    fun constraint(name: String, constraint: String) = this.apply {
        constraints.add("$name $constraint")
    }

    fun build(): Query {

        val sb = StringBuilder("CREATE TABLE")

        if (ifNotExists) {
            sb.append(" IF NOT EXISTS")
        }

        sb.append(" $table (")
        sb.append(columns.joinToString(", "))

        if (constraints.isNotEmpty()) {
            sb.append(", ")
            sb.append(constraints.joinToString(separator = ", ") { "CONSTRAINT $it" })
        }

        sb.append(")")

        return Query(sb.toString())
    }
}
