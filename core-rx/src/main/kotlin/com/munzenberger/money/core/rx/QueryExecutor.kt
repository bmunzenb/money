package com.munzenberger.money.core.rx

import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.TransactionQueryExecutor
import io.reactivex.Completable

fun QueryExecutor.observableTransaction(block: (TransactionQueryExecutor) -> Unit): Completable {

    val tx = createTransaction()

    return Completable.fromAction { block.invoke(tx) }
            .doOnComplete { tx.commit() }
            .doOnError { tx.rollback() }
}
