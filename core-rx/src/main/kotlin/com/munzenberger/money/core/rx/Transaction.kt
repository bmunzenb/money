package com.munzenberger.money.core.rx

import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transaction
import io.reactivex.Observable

fun Transaction.Companion.observableTransaction(identity: Long, database: ObservableMoneyDatabase): Observable<Transaction> =
        database.onUpdate.flatMap { Observable.create<Transaction> {
            when (val value = get(identity, database)) {
                null -> it.onError(PersistableNotFoundException(Transaction::class, identity))
                else -> it.onNext(value)
            }
        } }

fun Transaction.Companion.observableTransactionList(database: ObservableMoneyDatabase): Observable<List<Transaction>> =
        database.onUpdate.flatMap { Observable.fromCallable { getAll(database) } }
