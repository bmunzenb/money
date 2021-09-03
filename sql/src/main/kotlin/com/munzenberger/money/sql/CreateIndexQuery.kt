package com.munzenberger.money.sql

fun QueryExecutor.createIndex(name: String, table: String, block: IndexDefinition.() -> Unit) {
    execute(createIndexQuery(name, table, block))
}

fun createIndexQuery(name: String, table: String, block: IndexDefinition.() -> Unit): Query {
    val def = IndexDefinition(name, table)
    def.block()
    return def.toQuery()
}

class IndexDefinition(name: String, table: String) {

    private val builder = CreateIndexQueryBuilder(name, table)

    fun unique() {
        builder.unique()
    }

    fun column(name: String) {
        builder.column(name)
    }

    fun toQuery() = builder.build()
}
