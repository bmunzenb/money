package com.munzenberger.money.core.model

import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.eq

abstract class Model(var identity: Long? = null)

abstract class ModelQueryBuilder<M : Model> {

    abstract val table: String

    abstract val identityColumn: String

    abstract fun setValues(settable: SettableQueryBuilder<*>, model: M)

    open fun select() = Query.selectFrom(table)
            .orderBy(identityColumn)

    open fun select(identity: Long) = Query.selectFrom(table)
            .where(identityColumn.eq(identity))

    fun insert(model: M) = Query.insertInto(table)
            .also { setValues(it, model) }

    fun update(model: M) = Query.update(table)
            .also { setValues(it, model) }
            .where(identityColumn.eq(model.identity!!))

    fun delete(model: M) = Query.deleteFrom(table)
            .where(identityColumn.eq(model.identity!!))
}
