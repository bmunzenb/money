package com.munzenberger.money.sql

import java.sql.Connection
import java.util.logging.Level
import java.util.logging.Logger

class ConnectionQueryExecutor(private val connection: Connection) : QueryExecutor {

    override fun execute(query: Query) =
        SQLExecutor.execute(connection, query.sql, query.parameters)

    override fun executeQuery(query: Query, handler: ResultSetHandler?) =
        SQLExecutor.executeQuery(connection, query.sql, query.parameters, handler)

    override fun executeUpdate(query: Query, handler: ResultSetHandler?) =
        SQLExecutor.executeUpdate(connection, query.sql, query.parameters, handler)

    override fun createTransaction(): TransactionQueryExecutor =
        ConnectionTransactionQueryExecutor(connection, this)
}

private class ConnectionTransactionQueryExecutor(
    private val connection: Connection,
    private val executor: QueryExecutor
) : TransactionQueryExecutor, QueryExecutor by executor {

    private val logger = Logger.getLogger(ConnectionTransactionQueryExecutor::class.java.name)
    private val level = Level.FINE

    private val rollbackListeners = mutableListOf<Runnable>()
    private var autoCommit = connection.autoCommit

    init {
        logger.log(level, "Start transaction")
        autoCommit = connection.autoCommit
        connection.autoCommit = false
    }

    override fun createTransaction() =
        NestedTransactionQueryExecutor(0, this)

    override fun commit() {
        logger.log(level, "Commit transaction")
        connection.commit()
        connection.autoCommit = autoCommit
    }

    override fun rollback() {
        logger.log(level, "Rollback transaction")
        connection.rollback()
        connection.autoCommit = autoCommit
        rollbackListeners.forEach { it.run() }
        rollbackListeners.clear()
    }

    override fun addRollbackListener(listener: Runnable) {
        // treat the listeners as a LIFO queue
        rollbackListeners.add(0, listener)
    }
}

private class NestedTransactionQueryExecutor(
    private val nest: Int,
    private val tx: TransactionQueryExecutor
) : TransactionQueryExecutor, QueryExecutor by tx {

    private val logger = Logger.getLogger(NestedTransactionQueryExecutor::class.java.name)
    private val level = Level.FINE

    init {
        logger.log(level, "Start nested transaction: $nest")
    }

    override fun createTransaction() =
        NestedTransactionQueryExecutor(nest + 1, tx)

    override fun commit() {
        logger.log(level, "Delayed commit for nested transaction: $nest")
    }

    override fun rollback() {
        logger.log(level, "Delayed rollback for nested transaction: $nest")
    }

    override fun addRollbackListener(listener: Runnable) {
        tx.addRollbackListener(listener)
    }
}
