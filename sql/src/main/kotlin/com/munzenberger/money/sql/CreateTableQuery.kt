package com.munzenberger.money.sql

fun QueryExecutor.createTable(name: String, definition: TableDefinition.() -> Unit) {
    execute(createTableQuery(name, definition))
}

fun createTableQuery(name: String, block: TableDefinition.() -> Unit): Query {
    val def = TableDefinition(name)
    def.block()
    return def.toQuery()
}

class TableDefinition(name: String) {

    private val builder = CreateTableQueryBuilder(name)

    fun ifNotExists() = builder.ifNotExists()

    fun column(name: String, type: String, block: ColumnProperties.() -> Unit = {}) {

        val column = ColumnProperties()
        column.block()

        when (val r = column.reference) {
            null -> builder.column(name, type)
            else -> builder.columnWithReference(name, type, r.first, r.second)
        }
    }

    fun constraint(name: String, constraint: String) {
        builder.constraint(name, constraint)
    }

    internal fun toQuery(): Query = builder.build()
}

class ColumnProperties {

    internal var reference: Pair<String, String>? = null

    fun references(table: String, column: String) {
        reference = table to column
    }
}
