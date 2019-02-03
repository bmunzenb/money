package com.munzenberger.money.core

import com.munzenberger.money.core.model.Model
import com.munzenberger.money.core.model.ModelQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Completable

abstract class Persistable<M : Model>(
        protected val model: M,
        protected val modelQueryBuilder: ModelQueryBuilder<M>,
        protected val executer: QueryExecutor
) {

    val identity: Long?
        get() = model.identity

    fun save() = if (model.identity == null) insert() else update()

    protected fun insert() = Completable.create {

        val query = modelQueryBuilder.insert(model)
        val handler = IdentityResultSetHandler()

        executer.executeUpdate(query, handler)

        model.identity = handler.identity

        it.onComplete()
    }

    protected fun update() = Completable.create {

        val query = modelQueryBuilder.update(model)

        executer.executeUpdate(query)

        it.onComplete()
    }

    fun delete() = Completable.create {

        val query = modelQueryBuilder.delete(model)

        executer.executeUpdate(query)

        model.identity = null

        it.onComplete()
    }
}
