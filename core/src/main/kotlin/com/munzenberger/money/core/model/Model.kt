package com.munzenberger.money.core.model

import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.SettableQueryBuilder
import com.munzenberger.money.sql.eq

abstract class Model(var identity: Long? = null)

abstract class Repository<M : Model> {

    abstract val table: String

    abstract val identityColumn: String

    abstract fun setValues(settable: SettableQueryBuilder<*>, model: M)

    fun select() = Query.selectFrom(table)
            .orderBy(identityColumn)
            .build()

    fun select(identity: Long) = Query.selectFrom(table)
            .where(identityColumn.eq(identity))
            .build()

    fun insert(model: M) = Query.insertInto(table)
            .also { setValues(it, model) }
            .build()

    fun update(model: M) = Query.update(table)
            .also { setValues(it, model) }
            .where(identityColumn.eq(model.identity!!))
            .build()

    fun delete(model: M) = Query.deleteFrom(table)
            .where(identityColumn.eq(model.identity!!))
            .build()
}
