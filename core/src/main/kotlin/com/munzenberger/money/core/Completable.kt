package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.TransactionQueryExecutor
import io.reactivex.Completable

fun QueryExecutor.transaction(block: (tx: TransactionQueryExecutor) -> Completable): Completable =
    createTransaction().let { tx ->
        block.invoke(tx)
                .doOnComplete { tx.commit() }
                .doOnError { tx.rollback() }
    }
