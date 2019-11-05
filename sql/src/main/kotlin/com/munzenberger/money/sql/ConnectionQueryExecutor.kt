package com.munzenberger.money.sql

import java.sql.Connection
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

open class ConnectionQueryExecutor(private val connection: Connection) : QueryExecutor {

    override fun execute(query: Query) =
        SQLExecutor.execute(connection, query.sql, query.parameters)

    override fun executeQuery(query: Query, handler: ResultSetHandler?) =
        SQLExecutor.executeQuery(connection, query.sql, query.parameters, handler)

    override fun executeUpdate(query: Query, handler: ResultSetHandler?) =
        SQLExecutor.executeUpdate(connection, query.sql, query.parameters, handler)

    private val transactionManager by lazy {
        ManagedTransactionQueryExecutor(connection, this)
    }

    override fun createTransaction() = transactionManager.createTransaction()
}

private class ManagedTransactionQueryExecutor(
        private val connection: Connection,
        executor: QueryExecutor
) : TransactionQueryExecutor, QueryExecutor by executor {

    private val logger = Logger.getLogger(ManagedTransactionQueryExecutor::class.java.name)

    private var autoCommit = connection.autoCommit
    private var semaphore = AtomicInteger(0)

    @Synchronized
    override fun createTransaction(): TransactionQueryExecutor {

        when (val n = semaphore.getAndIncrement()) {
            0 -> {
                logger.fine("Start transaction")
                autoCommit = connection.autoCommit
                connection.autoCommit = false
            }
            else -> logger.fine("Start nested transaction: $n")
        }

        return this
    }

    @Synchronized
    override fun commit() {

        when (val n = semaphore.decrementAndGet()) {
            0 -> {
                logger.fine("Commit transaction")
                connection.commit()
                connection.autoCommit = autoCommit
            }
            else -> logger.fine("Delayed commit for nested transaction: $n")
        }
    }

    @Synchronized
    override fun rollback() {

        when (val n = semaphore.decrementAndGet()) {
            0 -> {
                logger.fine("Rollback transaction")
                connection.rollback()
                connection.autoCommit = autoCommit
            }
            else -> logger.fine("Delayed rollback for nested transaction: $n")
        }
    }
}
