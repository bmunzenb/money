package com.munzenberger.money.app.database

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.TransactionQueryExecutor
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject

class ObservableMoneyDatabase(private val database: MoneyDatabase) : MoneyDatabase by database {

    private val updateSubject = BehaviorSubject.createDefault(Unit)

    val onUpdate: Observable<Unit> = updateSubject

    override fun executeUpdate(query: Query, handler: ResultSetHandler?): Int {
        return database.executeUpdate(query, handler).also {
            updateSubject.onNext(Unit)
        }
    }

    override fun execute(query: Query): Boolean {
        return database.execute(query).also {
            when (it) { false -> updateSubject.onNext(Unit) }
        }
    }

    override fun createTransaction(): TransactionQueryExecutor =
            ObservableTransactionQueryExecutor(database.createTransaction(), updateSubject)

    override fun close() {
        database.close().also {
            updateSubject.onComplete()
        }
    }
}

private class ObservableTransactionQueryExecutor(
        private val executor: TransactionQueryExecutor,
        private val subject: Observer<Unit>? = null
) : TransactionQueryExecutor by executor {

    override fun createTransaction(): TransactionQueryExecutor {
        return ObservableTransactionQueryExecutor(executor.createTransaction())
    }

    override fun commit() {
        executor.commit()
        subject?.onNext(Unit)
    }
}
