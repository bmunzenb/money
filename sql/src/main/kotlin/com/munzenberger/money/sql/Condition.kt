package com.munzenberger.money.sql

data class Condition(
    val clause: String,
    val parameters: List<Any?> = emptyList(),
) {
    companion object {
        fun eq(
            column: String,
            value: Any?,
        ) = Condition("$column = ?", listOf(value))

        fun greaterThan(
            column: String,
            value: Any,
        ) = Condition("$column > ?", listOf(value))

        fun greaterThanOrEqualTo(
            column: String,
            value: Any,
        ) = Condition("$column >= ?", listOf(value))

        fun lessThan(
            column: String,
            value: Any,
        ) = Condition("$column < ?", listOf(value))

        fun lessThanOrEqualTo(
            column: String,
            value: Any,
        ) = Condition("$column <= ?", listOf(value))

        fun isNull(column: String) = Condition("$column IS NULL")

        fun isNotNull(column: String) = Condition("$column IS NOT NULL")

        fun inGroup(
            column: String,
            values: List<Any>,
        ) = Condition("$column IN (${values.map { '?' }.joinToString(", ")})", values)

        fun inGroup(
            column: String,
            vararg values: Any,
        ) = inGroup(column, values.toList())

        fun notInGroup(
            column: String,
            values: List<Any>,
        ) = Condition("$column NOT IN (${values.map { '?' }.joinToString(", ")})", values)

        fun notInGroup(
            column: String,
            vararg values: Any,
        ) = notInGroup(column, values.toList())
    }

    infix fun or(condition: Condition?) =
        when (condition) {
            null -> this
            else -> Condition("($clause) OR (${condition.clause})", parameters.plus(condition.parameters))
        }

    infix fun and(condition: Condition?) =
        when (condition) {
            null -> this
            else -> Condition("($clause) AND (${condition.clause})", parameters.plus(condition.parameters))
        }
}

fun String.eq(value: Int?) = Condition.eq(this, value)

fun String.eq(value: Long?) = Condition.eq(this, value)

fun String.eq(value: String?) = Condition.eq(this, value)

fun String.eq(value: Boolean?) = Condition.eq(this, value)

fun String.greaterThan(value: Any) = Condition.greaterThan(this, value)

fun String.greaterThanOrEqualTo(value: Any) = Condition.greaterThanOrEqualTo(this, value)

fun String.lessThan(value: Any) = Condition.lessThan(this, value)

fun String.lessThanOrEqualTo(value: Any) = Condition.lessThanOrEqualTo(this, value)

fun String.isNull() = Condition.isNull(this)

fun String.isNotNull() = Condition.isNotNull(this)

fun String.inGroup(values: List<Any>) = Condition.inGroup(this, values)

fun String.inGroup(vararg values: Any) = Condition.inGroup(this, *values)

fun String.notInGroup(values: List<Any>) = Condition.notInGroup(this, values)

fun String.notInGroup(vararg values: Any) = Condition.notInGroup(this, *values)
