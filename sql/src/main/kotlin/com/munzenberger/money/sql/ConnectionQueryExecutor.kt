package com.munzenberger.money.sql

import java.sql.Connection
import java.util.concurrent.atomic.AtomicInteger

open class ConnectionQueryExecutor(private val connection: Connection) : QueryExecutor {

    override fun execute(query: Query) {
        SQLExecutor.execute(connection, query.sql, query.parameters)
    }

    override fun executeQuery(query: Query, handler: ResultSetHandler?) {
        SQLExecutor.executeQuery(connection, query.sql, query.parameters, handler)
    }

    override fun executeUpdate(query: Query, handler: ResultSetHandler?) {
        SQLExecutor.executeUpdate(connection, query.sql, query.parameters, handler)
    }

    private val transactionManager by lazy {
        ManagedTransactionQueryExecutor(connection, this)
    }

    override fun createTransaction() = transactionManager.createTransaction()
}

private class ManagedTransactionQueryExecutor(
        private val connection: Connection,
        executor: QueryExecutor
) : TransactionQueryExecutor, QueryExecutor by executor {

    private var autoCommit = connection.autoCommit
    private var semaphore = AtomicInteger(0)

    @Synchronized
    override fun createTransaction(): TransactionQueryExecutor {

        if (semaphore.getAndIncrement() == 0) {
            autoCommit = connection.autoCommit
            connection.autoCommit = false
        }

        return this
    }

    @Synchronized
    override fun commit() {

        if (semaphore.decrementAndGet() == 0) {
            connection.commit()
            connection.autoCommit = autoCommit
        }
    }

    @Synchronized
    override fun rollback() {

        if (semaphore.decrementAndGet() == 0) {
            connection.rollback()
            connection.autoCommit = autoCommit
        }
    }
}
