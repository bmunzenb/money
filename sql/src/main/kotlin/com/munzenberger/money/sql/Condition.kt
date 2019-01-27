package com.munzenberger.money.sql

data class Condition(val clause: String, val parameters: List<Any>) {

    companion object {

        fun eq(column: String, value: Any) =
                Condition("$column = ?", listOf(value))

        fun greaterThan(column: String, value: Any) =
                Condition("$column > ?", listOf(value))

        fun greaterThanOrEquals(column: String, value: Any) =
                Condition("$column >= ?", listOf(value))

        fun lessThan(column: String, value: Any) =
                Condition("$column < ?", listOf(value))

        fun lessThanOrEquals(column: String, value: Any) =
                Condition("$column <= ?", listOf(value))

        fun isNull(column: String) =
                Condition("$column IS NULL", listOf())

        fun isNotNull(column: String) =
                Condition("$column IS NOT NULL", listOf())

        fun inGroup(column: String, values: List<Any>) =
                Condition("$column IN (${values.map { '?' }.joinToString(", ")})", values)

        fun notInGroup(column: String, values: List<Any>) =
                Condition("$column NOT IN (${values.map { '?' }.joinToString(", ")})", values)
    }

    fun or(condition: Condition) =
            Condition("($clause) OR (${condition.clause})", parameters.plus(condition.parameters))

    fun and(condition: Condition) =
            Condition("($clause) AND (${condition.clause})", parameters.plus(condition.parameters))
}

fun String.eq(value: Any) = Condition.eq(this, value)

fun String.greaterThan(value: Any) = Condition.greaterThan(this, value)

fun String.greaterThanOrEquals(value: Any) = Condition.greaterThanOrEquals(this, value)

fun String.lessThan(value: Any) = Condition.lessThan(this, value)

fun String.lessThanOrEquals(value: Any) = Condition.lessThanOrEquals(this, value)

fun String.isNull() = Condition.isNull(this)

fun String.isNotNull() = Condition.isNotNull(this)

fun String.inGroup(values: List<Any>) = Condition.inGroup(this, values)

fun String.notInGroup(values: List<Any>) = Condition.notInGroup(this, values)
