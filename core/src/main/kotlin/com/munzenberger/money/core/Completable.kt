package com.munzenberger.money.core

import com.munzenberger.money.sql.TransactionQueryExecutor
import io.reactivex.Completable

fun Completable.withTransaction(tx: TransactionQueryExecutor): Completable = this
        .doOnComplete { tx.commit() }
        .doOnError { tx.rollback() }
