package com.munzenberger.money.core.model

import com.munzenberger.money.sql.*

abstract class Model(var identity: Long? = null)

abstract class Table<M : Model> {

    abstract val name: String

    abstract val identityColumn: String

    abstract fun setValues(settable: SettableQueryBuilder<*>, model: M)

    open fun applyJoins(select: SelectQueryBuilder) {}

    fun select() = Query.selectFrom(name)
            .also { applyJoins(it) }

    fun select(identity: Long) = Query.selectFrom(name)
            .also { applyJoins(it) }
            .where(identityColumn.eq(identity))

    fun insert(model: M) = Query.insertInto(name)
            .also { setValues(it, model) }

    fun update(model: M) = Query.update(name)
            .also { setValues(it, model) }
            .where(identityColumn.eq(model.identity!!))

    fun delete(model: M) = Query.deleteFrom(name)
            .where(identityColumn.eq(model.identity!!))

    protected fun SelectQueryBuilder.leftJoin(leftColumn: String, right: Table<*>) =
            leftJoin(name, leftColumn, right.name, right.identityColumn).apply { right.applyJoins(this) }
}

fun Query.Companion.selectFrom(table: Table<*>) =
        selectFrom(table.name)

fun SelectQueryBuilder.innerJoin(leftTable: Table<*>, leftColumn: String, rightTable: Table<*>, rightColumn: String) =
        innerJoin(leftTable.name, leftColumn, rightTable.name, rightColumn)

fun SelectQueryBuilder.leftJoin(leftTable: Table<*>, leftColumn: String, rightTable: Table<*>, rightColumn: String) =
        leftJoin(leftTable.name, leftColumn, rightTable.name, rightColumn)

fun SelectQueryBuilder.rightJoin(leftTable: Table<*>, leftColumn: String, rightTable: Table<*>, rightColumn: String) =
        rightJoin(leftTable.name, leftColumn, rightTable.name, rightColumn)
