package com.munzenberger.money.sql

data class Column(val table: String, val name: String) {
    override fun toString() = "$table.$name"
}

fun Column.eq(value: Any) = toString().eq(value)

fun Column.greaterThan(value: Any) = toString().greaterThan(value)

fun Column.greaterThanOrEqualTo(value: Any) = toString().greaterThanOrEqualTo(value)

fun Column.lessThan(value: Any) = toString().lessThan(value)

fun Column.lessThanOrEqualTo(value: Any) = toString().lessThanOrEqualTo(value)

fun Column.isNull() = toString().isNull()

fun Column.isNotNull() = toString().isNotNull()

fun Column.inGroup(values: List<Any>) = toString().inGroup(values)

fun Column.notInGroup(values: List<Any>) = toString().notInGroup(values)

fun SelectQueryBuilder.columns(columns: List<Column>) = this.apply {
    cols(columns.map { it.toString() })
}

fun SelectQueryBuilder.columns(vararg columns: Column) = this.apply {
    cols(columns.map { it.toString() })
}

fun SelectQueryBuilder.innerJoin(leftColumn: Column, rightColumn: Column) = this.apply {
    innerJoin(rightColumn.table, leftColumn.name, rightColumn.name)
}

fun SelectQueryBuilder.leftJoin(leftColumn: Column, rightColumn: Column) = this.apply {
    leftJoin(rightColumn.table, leftColumn.name, rightColumn.name)
}

fun SelectQueryBuilder.rightJoin(leftColumn: Column, rightColumn: Column) = this.apply {
    rightJoin(rightColumn.table, leftColumn.name, rightColumn.name)
}

fun SelectQueryBuilder.orderBy(column: Column, descending: Boolean = false) = this.apply {
    orderBy(column.toString(), descending)
}

fun CreateTableQueryBuilder.column(column: Column, type: String) = this.apply {
    column(column.name, type)
}

fun CreateTableQueryBuilder.columnWithReference(column: Column, type: String, refColumn: Column) = this.apply {
    columnWithReference(column.name, type, refColumn.table, refColumn.name)
}
