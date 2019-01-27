package com.munzenberger.money.sql

data class Query(val sql: String, val parameters: List<Any> = emptyList()) {

    companion object {

        fun selectFrom(table: String) = SelectQueryBuilder(table)

        fun createTable(table: String) = CreateTableQueryBuilder(table)
    }
}
