package com.munzenberger.money.core

import com.munzenberger.money.core.model.Model
import com.munzenberger.money.core.model.Table
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import io.reactivex.Completable
import io.reactivex.Single
import kotlin.reflect.KClass

abstract class Persistable<M : Model>(
        protected val model: M,
        private val table: Table<M>) {

    val identity: Long?
        get() = model.identity

    open fun save(executor: QueryExecutor) = if (model.identity == null) insert(executor) else update(executor)

    private fun insert(executor: QueryExecutor) = Completable.create {

        val query = table.insert(model).build()
        val handler = IdentityResultSetHandler()

        executor.executeUpdate(query, handler)

        model.identity = handler.identity

        it.onComplete()
    }

    private fun update(executor: QueryExecutor) = Completable.create {

        val query = table.update(model).build()

        executor.executeUpdate(query)

        it.onComplete()
    }

    fun delete(executor: QueryExecutor) = Completable.create {

        val query = table.delete(model).build()

        executor.executeUpdate(query)

        model.identity = null

        it.onComplete()
    }

    companion object {

        internal fun <M : Model, P : Persistable<M>> getAll(
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>
        ) = Single.create<List<P>> {

            val query = table.select().orderBy(table.identityColumn).build()
            val list = executor.getList(query, mapper)

            it.onSuccess(list)
        }

        internal fun <M : Model, P : Persistable<M>> get(
                identity: Long,
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>,
                clazz: KClass<P>
        ) = Single.create<P> {

            val query = table.select(identity).build()
            val persistable = executor.getFirst(query, mapper)

            when (persistable) {
                is P -> it.onSuccess(persistable)
                else -> it.onError(PersistableNotFoundException(clazz, identity))
            }
        }

        internal fun getIdentity(persistable: Persistable<*>?, executor: QueryExecutor, block: (Long?) -> Unit) = when {

            persistable == null ->
                Completable.complete().doOnComplete { block(null) }

            persistable.identity == null ->
                persistable.save(executor).doOnComplete { block(persistable.identity) }

            else ->
                Completable.complete().doOnComplete { block(persistable.identity) }
        }
    }

    internal fun completableChain(head: Completable, vararg tail: Completable) =
            tail.fold(head) { chain, next -> chain.andThen(next) }
}
