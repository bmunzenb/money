package com.munzenberger.money.sql

import java.lang.StringBuilder

class DeleteQueryBuilder(private val table: String) {

    private var where: Condition? = null

    fun where(condition: Condition) = this.apply {
        this.where = condition
    }

    fun build(): Query {

        val sb = StringBuilder("DELETE FROM $table")
        val params = mutableListOf<Any>()

        where?.run {
            sb.append(" WHERE $clause")
            params.addAll(parameters)
        }

        return Query(sb.toString(), params)
    }
}
