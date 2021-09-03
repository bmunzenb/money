package com.munzenberger.money.sql

data class Query(val sql: String, val parameters: List<Any?> = emptyList()) {

    companion object {

        // TODO: convert to DSL
        fun selectFrom(table: String) = SelectQueryBuilder(table)

        // TODO: convert to DSL
        fun insertInto(table: String) = InsertQueryBuilder(table)

        // TODO: convert to DSL
        fun update(table: String) = UpdateQueryBuilder(table)

        // TODO: convert to DSL
        fun deleteFrom(table: String) = DeleteQueryBuilder(table)
    }
}
