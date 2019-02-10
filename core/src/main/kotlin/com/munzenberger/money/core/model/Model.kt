package com.munzenberger.money.core.model

import com.munzenberger.money.sql.*

abstract class Model(var identity: Long? = null)

abstract class ModelQueryBuilder<M : Model> {

    abstract val table: String

    abstract val identityColumn: String

    abstract fun setValues(settable: SettableQueryBuilder<*>, model: M)

    open fun applyJoins(select: SelectQueryBuilder) {}

    fun select() = Query.selectFrom(table)
            .also { applyJoins(it) }
            .orderBy(identityColumn)

    fun select(identity: Long) = Query.selectFrom(table)
            .also { applyJoins(it) }
            .where(identityColumn.eq(identity))

    fun insert(model: M) = Query.insertInto(table)
            .also { setValues(it, model) }

    fun update(model: M) = Query.update(table)
            .also { setValues(it, model) }
            .where(identityColumn.eq(model.identity!!))

    fun delete(model: M) = Query.deleteFrom(table)
            .where(identityColumn.eq(model.identity!!))

    protected fun SelectQueryBuilder.leftJoin(leftColumn: String, right: ModelQueryBuilder<*>) =
            leftJoin(table, leftColumn, right.table, right.identityColumn).apply { right.applyJoins(this) }
}
