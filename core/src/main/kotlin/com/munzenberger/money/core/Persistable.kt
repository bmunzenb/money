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

    open fun save(executor: QueryExecutor) = Completable.fromAction {
        when (model.identity) {
            null -> insert(executor)
            else -> update(executor)
        }
    }

    private fun insert(executor: QueryExecutor) {

        val query = table.insert(model).build()
        val handler = IdentityResultSetHandler()

        executor.executeUpdate(query, handler)

        model.identity = handler.identity
    }

    private fun update(executor: QueryExecutor) {

        val query = table.update(model).build()

        executor.executeUpdate(query)
    }

    open fun delete(executor: QueryExecutor) = Completable.fromAction {

        val query = table.delete(model).build()

        executor.executeUpdate(query)

        model.identity = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Persistable<*>

        if (model != other.model) return false
        if (table != other.table) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        result = 31 * result + table.hashCode()
        return result
    }

    companion object {

        internal fun <M : Model, P : Persistable<M>> getAll(
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>,
                orderBy: String = table.identityColumn,
                descending: Boolean = false
        ) = Single.fromCallable {
            table.select().orderBy(orderBy, descending).build().let {
                executor.getList(it, mapper)
            }
        }

        internal fun <M : Model, P : Persistable<M>> get(
                identity: Long,
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>,
                clazz: KClass<P>
        ) = Single.create<P> {

            val query = table.select(identity).build()

            when (val persistable = executor.getFirst(query, mapper)) {
                is P -> it.onSuccess(persistable)
                else -> it.onError(PersistableNotFoundException(clazz, identity))
            }
        }
    }
}

fun Persistable<*>?.getIdentity(executor: QueryExecutor, block: (Long?) -> Unit): Completable = when {

    this == null ->
        Completable.complete().doOnComplete { block.invoke(null) }

    identity == null ->
        save(executor).doOnComplete { block.invoke(identity) }

    else ->
        Completable.complete().doOnComplete { block.invoke(identity) }
}
