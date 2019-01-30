package com.munzenberger.money.core

import com.munzenberger.money.core.model.Model
import com.munzenberger.money.core.model.Repository
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Completable

abstract class Persistable<M : Model>(
        protected val model: M,
        protected val repository: Repository<M>,
        protected val executer: QueryExecutor
) {

    val identity: Long?
        get() = model.identity

    fun save() = if (model.identity == null) insert() else update()

    protected fun insert() = Completable.create {

        val query = repository.insert(model)
        val identity = IdentityResultSetHandler()

        executer.executeUpdate(query, identity)

        model.identity = identity.result

        it.onComplete()
    }

    protected fun update() = Completable.create {

        val query = repository.update(model)

        executer.executeUpdate(query)

        it.onComplete()
    }

    fun delete() = Completable.create {

        val query = repository.delete(model)

        executer.executeUpdate(query)

        model.identity = null

        it.onComplete()
    }
}
