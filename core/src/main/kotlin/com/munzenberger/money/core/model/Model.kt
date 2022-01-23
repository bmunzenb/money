package com.munzenberger.money.core.model

import com.munzenberger.money.sql.*
import java.sql.ResultSet

abstract class Model(var identity: Long? = null)

abstract class Table<M : Model> {

    abstract val tableName: String

    abstract val identityColumn: String

    abstract fun setValues(settable: SettableQueryBuilder<*>, model: M)

    abstract fun getValues(resultSet: ResultSet, model: M)

    open fun applyJoins(select: SelectQueryBuilder) {}

    fun select() = Query.selectFrom(tableName)
            .also { applyJoins(it) }

    fun select(identity: Long) = Query.selectFrom(tableName)
            .also { applyJoins(it) }
            .where(identityColumn.eq(identity))

    fun insert(model: M) = Query.insertInto(tableName)
            .also { setValues(it, model) }

    fun update(model: M) = Query.update(tableName)
            .also { setValues(it, model) }
            .where(identityColumn.eq(model.identity!!))

    fun delete(model: M) = Query.deleteFrom(tableName)
            .where(identityColumn.eq(model.identity!!))

    protected fun SelectQueryBuilder.leftJoin(leftColumn: String, right: Table<*>) =
            leftJoin(tableName, leftColumn, right.tableName, right.identityColumn).apply { right.applyJoins(this) }
}

fun Query.Companion.selectFrom(table: Table<*>) =
        selectFrom(table.tableName)

fun SelectQueryBuilder.innerJoin(leftTable: Table<*>, leftColumn: String, rightTable: Table<*>, rightColumn: String) =
        innerJoin(leftTable.tableName, leftColumn, rightTable.tableName, rightColumn)

fun SelectQueryBuilder.leftJoin(leftTable: Table<*>, leftColumn: String, rightTable: Table<*>, rightColumn: String) =
        leftJoin(leftTable.tableName, leftColumn, rightTable.tableName, rightColumn)

fun SelectQueryBuilder.rightJoin(leftTable: Table<*>, leftColumn: String, rightTable: Table<*>, rightColumn: String) =
        rightJoin(leftTable.tableName, leftColumn, rightTable.tableName, rightColumn)
