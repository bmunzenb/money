package com.munzenberger.money.core.rx

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.TransactionQueryExecutor
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

class ObservableMoneyDatabase(private val database: MoneyDatabase) : MoneyDatabase by database {

    private val updatePublisher = PublishSubject.create<Unit>()

    val updateObservable: Observable<Unit> = updatePublisher

    override fun executeUpdate(query: Query, handler: ResultSetHandler?): Int {
        return database.executeUpdate(query, handler).also {
            updatePublisher.onNext(Unit)
        }
    }

    override fun execute(query: Query): Boolean {
        return database.execute(query).also {
            when (it) { false -> updatePublisher.onNext(Unit) }
        }
    }

    override fun createTransaction(): TransactionQueryExecutor =
            ObservableTransactionQueryExecutor(database.createTransaction(), updatePublisher)

    override fun close() {
        database.close().also {
            updatePublisher.onComplete()
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
