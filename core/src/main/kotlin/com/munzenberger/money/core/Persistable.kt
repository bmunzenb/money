package com.munzenberger.money.core

import com.munzenberger.money.core.model.Model
import com.munzenberger.money.core.model.Table
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.TransactionQueryExecutor
import com.munzenberger.money.sql.transaction

interface Persistable {

    val identity: Long?

    fun save(executor: QueryExecutor)

    fun delete(executor: QueryExecutor)
}

abstract class AbstractPersistable<M : Model>(
        protected val model: M,
        private val table: Table<M>
) : Persistable {

    override val identity: Long?
        get() = model.identity

    override fun save(executor: QueryExecutor) {
        when (model.identity) {
            null -> insert(executor)
            else -> update(executor)
        }
    }

    private fun insert(executor: QueryExecutor) {

        executor.transaction { tx ->

            val query = table.insert(model).build()
            tx.executeUpdate(query)

            // TODO this may not be a safe way to get the identity of the inserted row
            // consider exposing the database dialect here and using it for a database-specific implementation
            val identityHandler = IdentityResultSetHandler()
            val getIdentity = table.select().cols("MAX(${table.identityColumn})").build()

            model.identity = tx.getFirst(getIdentity, identityHandler)
        }

        if (executor is TransactionQueryExecutor) {
            executor.addRollbackListener {
                model.identity = null
            }
        }
    }

    private fun update(executor: QueryExecutor) {

        val query = table.update(model).build()

        when (executor.executeUpdate(query)) {
            0 -> error("No rows updated for persistable.")
        }
    }

    override fun delete(executor: QueryExecutor) {

        if (model.identity != null) {

            val query = table.delete(model).build()

            when (executor.executeUpdate(query)) {
                0 -> error("No rows deleted for persistable.")
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

        other as AbstractPersistable<*>

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

        internal fun <M : Model, P : AbstractPersistable<M>> getAll(
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>
        ) = table.select().orderBy(table.identityColumn).build().let { executor.getList(it, mapper) }

        internal fun <M : Model, P : AbstractPersistable<M>> get(
                identity: Long,
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>
        ) = table.select(identity).build().let { executor.getFirst(it, mapper) }
    }
}

fun Persistable?.getIdentity(executor: QueryExecutor) =
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
