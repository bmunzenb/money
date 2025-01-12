package com.munzenberger.money.app.database

import com.munzenberger.money.app.concurrent.Executors
import com.munzenberger.money.app.observable.Observable
import com.munzenberger.money.app.observable.ObservableImpl
import com.munzenberger.money.app.observable.Subscription
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.TransactionQueryExecutor
import java.util.concurrent.Executor

class ObservableMoneyDatabase(private val database: MoneyDatabase) : MoneyDatabase by database, Observable {
    private val observable = ObservableImpl()

    override fun executeUpdate(query: Query): Int {
        return database.executeUpdate(query).also {
            observable.onChanged()
        }
    }

    override fun execute(query: Query): Boolean {
        return database.execute(query).also { isResults ->
            if (!isResults) {
                observable.onChanged()
            }
        }
    }

    override fun createTransaction(): TransactionQueryExecutor =
        ObservableTransactionQueryExecutor(database.createTransaction(), observable)

    override fun close() {
        database.close()
    }

    fun subscribe(block: Runnable): Subscription {
        return subscribe(Executors.PLATFORM, block)
    }

    override fun subscribe(
        executor: Executor,
        block: Runnable,
    ): Subscription {
        return observable.subscribe(executor, block)
    }
}

private class ObservableTransactionQueryExecutor(
    private val executor: TransactionQueryExecutor,
    private val observable: ObservableImpl? = null,
) : TransactionQueryExecutor by executor {
    override fun createTransaction(): TransactionQueryExecutor {
        return ObservableTransactionQueryExecutor(executor.createTransaction())
    }

    override fun commit() {
        executor.commit()
        observable?.onChanged()
    }
}
