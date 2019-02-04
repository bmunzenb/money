package com.munzenberger.money.core

import com.munzenberger.money.core.model.Model
import com.munzenberger.money.core.model.ModelQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import io.reactivex.Completable
import io.reactivex.Single
import kotlin.reflect.KClass

abstract class Persistable<M : Model>(
        protected val model: M,
        protected val modelQueryBuilder: ModelQueryBuilder<M>,
        protected val executor: QueryExecutor
) {

    val identity: Long?
        get() = model.identity

    fun save() = if (model.identity == null) insert() else update()

    private fun insert() = Completable.create {

        val query = modelQueryBuilder.insert(model)
        val handler = IdentityResultSetHandler()

        executor.executeUpdate(query, handler)

        model.identity = handler.identity

        it.onComplete()
    }

    private fun update() = Completable.create {

        val query = modelQueryBuilder.update(model)

        executor.executeUpdate(query)

        it.onComplete()
    }

    fun delete() = Completable.create {

        val query = modelQueryBuilder.delete(model)

        executor.executeUpdate(query)

        model.identity = null

        it.onComplete()
    }

    companion object {

        fun <M : Model, P : Persistable<M>> getAll(
                executor: QueryExecutor,
                queryBuilder: ModelQueryBuilder<M>,
                mapper: ResultSetMapper<P>
        ) = Single.create<List<P>> {

            val query = queryBuilder.select()
            val list = executor.getList(query, mapper)

            it.onSuccess(list)
        }

        fun <M : Model, P : Persistable<M>> get(
                identity: Long,
                executor: QueryExecutor,
                queryBuilder: ModelQueryBuilder<M>,
                mapper: ResultSetMapper<P>,
                clazz: KClass<P>
        ) = Single.create<P> {

            val query = queryBuilder.select(identity)
            val persistable = executor.getFirst(query, mapper)

            when (persistable) {
                is P -> it.onSuccess(persistable)
                else -> it.onError(PersistableNotFoundException(clazz, identity))
            }
        }
    }
}
