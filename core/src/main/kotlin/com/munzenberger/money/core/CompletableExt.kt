package com.munzenberger.money.core

import com.munzenberger.money.sql.TransactionQueryExecutor
import io.reactivex.Completable

fun Completable.withTransaction(tx: TransactionQueryExecutor): Completable = this
        .doOnComplete { tx.commit() }
        .doOnError { tx.rollback() }

fun concatAll(head: Completable, vararg tail: Completable) =
        tail.fold(head) { chain, next -> chain.concatWith(next) }
