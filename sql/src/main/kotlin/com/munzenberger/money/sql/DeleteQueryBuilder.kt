package com.munzenberger.money.sql

import java.lang.StringBuilder

fun deleteQuery(
    from: String,
    block: DeleteQueryBuilder.() -> Unit,
): Query {
    val deleteQueryBuilder = DeleteQueryBuilder(table = from)
    deleteQueryBuilder.block()
    return deleteQueryBuilder.build()
}

@DeleteQueryMarker
class DeleteQueryBuilder(private val table: String) : ConditionalQueryBuilder<DeleteQueryBuilder>() {
    override fun build(where: Condition?): Query {
        val sb = StringBuilder("DELETE FROM $table")
        val params = mutableListOf<Any?>()

        where?.run {
            sb.append(" WHERE $clause")
            params.addAll(parameters)
        }

        return Query(sb.toString(), params)
    }

    override fun instance() = this
}

@DslMarker
annotation class DeleteQueryMarker
