package com.munzenberger.money.core

import com.munzenberger.money.core.model.Model
import com.munzenberger.money.core.model.Table
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.TransactionQueryExecutor
import java.lang.IllegalStateException

abstract class Persistable<M : Model>(
        protected val model: M,
        private val table: Table<M>) {

    val identity: Long?
        get() = model.identity

    open fun save(executor: QueryExecutor) {
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

        if (executor is TransactionQueryExecutor) {
            executor.addRollbackListener {
                model.identity = null
            }
        }
    }

    private fun update(executor: QueryExecutor) {

        val query = table.update(model).build()

        when (executor.executeUpdate(query)) {
            0 -> throw IllegalStateException("No rows updated for persistable.")
        }
    }

    open fun delete(executor: QueryExecutor) {

        if (model.identity != null) {

            val query = table.delete(model).build()

            when (executor.executeUpdate(query)) {
                0 -> throw IllegalStateException("No rows deleted for persistable.")
            }

            val i = model.identity

            model.identity = null

            if (executor is TransactionQueryExecutor) {
                executor.addRollbackListener {
                    model.identity = i
                }
            }
        }
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
                mapper: ResultSetMapper<P>
        ) = table.select().orderBy(table.identityColumn).build().let { executor.getList(it, mapper) }

        internal fun <M : Model, P : Persistable<M>> get(
                identity: Long,
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>
        ) = table.select(identity).build().let { executor.getFirst(it, mapper) }
    }
}

fun Persistable<*>?.getIdentity(executor: QueryExecutor) =
        when {
            this == null ->
                null

            identity == null -> {
                save(executor)
                identity
            }

            else ->
                identity
        }
