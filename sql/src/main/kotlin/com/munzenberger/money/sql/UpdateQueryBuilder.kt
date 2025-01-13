package com.munzenberger.money.sql

import java.lang.StringBuilder

fun updateQuery(
    table: String,
    block: UpdateQueryBuilder.() -> Unit,
): Query {
    val updateQueryBuilder = UpdateQueryBuilder(table)
    updateQueryBuilder.block()
    return updateQueryBuilder.build()
}

@UpdateQueryMarker
class UpdateQueryBuilder(
    table: String,
) : SettableQueryBuilder<UpdateQueryBuilder>(table) {
    private var where: Condition? = null

    fun where(condition: Condition) =
        this.apply {
            this.where = condition
        }

    override fun instance() = this

    override fun build(
        table: String,
        parameters: Map<String, Any?>,
    ): Query {
        val sb = StringBuilder("UPDATE $table SET ")
        val params = mutableListOf<Any?>()

        sb.append(
            parameters.entries.joinToString(", ") {
                params.add(it.value)
                "${it.key} = ?"
            },
        )

        where?.run {
            sb.append(" WHERE $clause")
            params.addAll(this.parameters)
        }

        return Query(sb.toString(), params)
    }
}

@DslMarker
annotation class UpdateQueryMarker
