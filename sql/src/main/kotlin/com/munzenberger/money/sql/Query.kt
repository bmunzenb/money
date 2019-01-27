package com.munzenberger.money.sql

data class Query(val sql: String, val parameters: List<Any>) {

    companion object {

        fun selectFrom(table: String) = SelectQueryBuilder(table)
    }
}
