package com.munzenberger.money.core

import com.munzenberger.money.core.model.Model
import com.munzenberger.money.core.model.Table
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.TransactionQueryExecutor
import com.munzenberger.money.sql.transaction

interface MoneyEntity<I : Identity> {

    val identity: I?

    fun save(executor: QueryExecutor)

    fun delete(executor: QueryExecutor)
}

abstract class AbstractMoneyEntity<I : Identity, M : Model>(
        protected val model: M,
        private val table: Table<M>
) : MoneyEntity<I> {

    override fun save(executor: QueryExecutor) {
        when (val identity = model.identity) {
            null -> insert(executor)
            else -> update(identity, executor)
        }
    }

    private fun insert(executor: QueryExecutor) {

        executor.transaction { tx ->

            val query = table.insert(model)
            tx.executeUpdate(query)

            // TODO this may not be a safe way to get the identity of the inserted row
            // consider exposing the database dialect here and using it for a database-specific implementation
            val identityHandler = IdentityResultSetHandler()
            val getIdentity = table.select {
                cols("MAX(${table.identityColumn})")
            }

            model.identity = tx.getFirst(getIdentity, identityHandler)

            tx.addRollbackListener {
                model.identity = null
            }
        }
    }

    private fun update(identity: Long, executor: QueryExecutor) {

        val query = table.update(identity, model)

        when (executor.executeUpdate(query)) {
            0 -> error("No rows updated for entity.")
        }
    }

    override fun delete(executor: QueryExecutor) {

        model.identity?.let { identity ->

            val query = table.delete(identity)

            when (executor.executeUpdate(query)) {
                0 -> error("No rows deleted for entity.")
            }

            model.identity = null

            if (executor is TransactionQueryExecutor) {
                executor.addRollbackListener {
                    model.identity = identity
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractMoneyEntity<*, *>

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

        internal fun <I : Identity, M : Model, P : AbstractMoneyEntity<I, M>> getAll(
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>
        ) = table.select {
            orderBy(table.identityColumn)
        }.let { executor.getList(it, mapper) }

        internal fun <I : Identity, M : Model, P : AbstractMoneyEntity<I, M>> get(
                identity: I,
                executor: QueryExecutor,
                table: Table<M>,
                mapper: ResultSetMapper<P>
        ) = table.select(identity.value).let { executor.getFirst(it, mapper) }
    }
}

fun <I : Identity> MoneyEntity<I>?.getIdentity(executor: QueryExecutor) =
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
