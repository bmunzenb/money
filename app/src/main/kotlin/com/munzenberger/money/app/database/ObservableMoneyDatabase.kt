package com.munzenberger.money.app.database

import com.munzenberger.money.app.concurrent.Executors
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.TransactionQueryExecutor
import java.util.concurrent.Executor

class ObservableMoneyDatabase(private val database: MoneyDatabase) : MoneyDatabase by database, ObservableDatabase {

    private val observableDatabase = ObservableDatabaseImpl()

    override fun executeUpdate(query: Query, handler: ResultSetHandler?): Int {
        return database.executeUpdate(query, handler).also {
            observableDatabase.fireOnUpdate()
        }
    }

    override fun execute(query: Query): Boolean {
        return database.execute(query).also { isResults ->
            if (!isResults) { observableDatabase.fireOnUpdate() }
        }
    }

    override fun createTransaction(): TransactionQueryExecutor =
            ObservableTransactionQueryExecutor(database.createTransaction(), observableDatabase)

    override fun close() {
        database.close()
    }

    fun subscribeOnUpdate(block: Runnable): Subscription {
        return subscribeOnUpdate(Executors.PLATFORM, block)
    }

    override fun subscribeOnUpdate(executor: Executor, block: Runnable): Subscription {
        return observableDatabase.subscribeOnUpdate(executor, block)
    }
}

private class ObservableTransactionQueryExecutor(
        private val executor: TransactionQueryExecutor,
        private val observableDatabase: ObservableDatabaseImpl? = null
) : TransactionQueryExecutor by executor {

    override fun createTransaction(): TransactionQueryExecutor {
        return ObservableTransactionQueryExecutor(executor.createTransaction())
    }

    override fun commit() {
        executor.commit()
        observableDatabase?.fireOnUpdate()
    }
}
