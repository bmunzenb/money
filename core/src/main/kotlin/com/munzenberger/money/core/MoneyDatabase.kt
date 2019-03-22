package com.munzenberger.money.core

import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetHandler
import com.munzenberger.money.sql.TransactionQueryExecutor
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class MoneyDatabase(
        val name: String,
        val dialect: DatabaseDialect,
        private val executor: QueryExecutor
) : QueryExecutor by executor {

    private val updatePublisher = PublishSubject.create<Unit>()

    val updateObservable: Observable<Unit> = updatePublisher

    override fun executeUpdate(query: Query, handler: ResultSetHandler?): Int {
        return executor.executeUpdate(query, handler).also {
            updatePublisher.onNext(Unit)
        }
    }

    override fun createTransaction(): TransactionQueryExecutor =
            MoneyDatabaseTransactionExecutor(executor.createTransaction(), updatePublisher)

    open fun close() {
        updatePublisher.onComplete()
    }
}

private class MoneyDatabaseTransactionExecutor(
        private val executor: TransactionQueryExecutor,
        private val subject: Subject<Unit>
) : TransactionQueryExecutor by executor {

    override fun createTransaction(): TransactionQueryExecutor =
            MoneyDatabaseTransactionExecutor(executor.createTransaction(), subject)

    override fun commit() {
        executor.commit()
        subject.onNext(Unit)
    }
}
