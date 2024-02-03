package com.munzenberger.money.sql

import java.sql.Connection
import java.util.logging.Level
import java.util.logging.Logger

class ConnectionQueryExecutor(private val connection: Connection) : QueryExecutor {

    override fun execute(query: Query) =
        SQLExecutor.execute(connection, query.sql, query.parameters)

    override fun executeQuery(query: Query, handler: ResultSetHandler?) =
        SQLExecutor.executeQuery(connection, query.sql, query.parameters, handler)

    override fun executeUpdate(query: Query) =
        SQLExecutor.executeUpdate(connection, query.sql, query.parameters)

    override fun createTransaction(): TransactionQueryExecutor =
        ConnectionTransactionQueryExecutor(connection, this)
}

private class ConnectionTransactionQueryExecutor(
    private val connection: Connection,
    private val executor: QueryExecutor
) : TransactionQueryExecutor {

    private val logger = Logger.getLogger(ConnectionTransactionQueryExecutor::class.java.name)
    private val level = Level.FINE

    private val rollbackListeners = mutableListOf<Runnable>()
    private val initialAutoCommit = connection.autoCommit
    private var isClosed = false

    init {
        logger.log(level, "Start transaction")
        connection.autoCommit = false
    }

    override fun execute(query: Query): Boolean {
        assertNotClosed()
        return executor.execute(query)
    }

    override fun executeQuery(query: Query, handler: ResultSetHandler?) {
        assertNotClosed()
        executor.executeQuery(query, handler)
    }

    override fun executeUpdate(query: Query): Int {
        assertNotClosed()
        return executor.executeUpdate(query)
    }

    override fun createTransaction(): TransactionQueryExecutor {
        assertNotClosed()
        return NestedTransactionQueryExecutor(1, this)
    }

    override fun commit() {
        assertNotClosed()
        logger.log(level, "Commit transaction")
        connection.commit()
        close()
    }

    override fun rollback() {
        assertNotClosed()
        logger.log(level, "Rollback transaction")
        connection.rollback()
        rollbackListeners.forEach { it.run() }
        rollbackListeners.clear()
        close()
    }

    override fun addRollbackListener(listener: Runnable) {
        assertNotClosed()
        // treat the listeners as a LIFO queue
        rollbackListeners.add(0, listener)
    }

    private fun close() {
        connection.autoCommit = initialAutoCommit
        isClosed = true
    }

    private fun assertNotClosed() {
        if (isClosed) {
            error("Transaction is closed.")
        }
    }
}

private class NestedTransactionQueryExecutor(
    private val nest: Int,
    private val tx: TransactionQueryExecutor
) : TransactionQueryExecutor, QueryExecutor by tx {

    private val logger = Logger.getLogger(NestedTransactionQueryExecutor::class.java.name)
    private val level = Level.FINE

    init {
        logger.log(level, "Start nested transaction, level $nest")
    }

    override fun createTransaction() =
        NestedTransactionQueryExecutor(nest + 1, tx)

    override fun commit() {
        logger.log(level, "Delayed commit for nested transaction, level $nest")
    }

    override fun rollback() {
        logger.log(level, "Delayed rollback for nested transaction, level $nest")
    }

    override fun addRollbackListener(listener: Runnable) {
        tx.addRollbackListener(listener)
    }
}
