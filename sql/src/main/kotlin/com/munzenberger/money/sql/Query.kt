package com.munzenberger.money.sql

data class Query(
    val sql: String,
    val parameters: List<Any?> = emptyList(),
) {
    override fun toString(): String =
        when {
            parameters.isEmpty() -> sql
            else -> "$sql -> ${parameters.joinToString(prefix = "[", postfix = "]") {
                when (it) {
                    is String -> "'$it'"
                    else -> it.toString()
                }
            }}"
        }
}
