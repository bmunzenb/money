package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.PersistableNotFoundException
import io.reactivex.Observable

fun Payee.Companion.observablePayee(identity: Long, database: ObservableMoneyDatabase): Observable<Payee> =
        database.onUpdate.flatMap { Observable.create<Payee> {
            when (val value = get(identity, database)) {
                null -> it.onError(PersistableNotFoundException(Payee::class, identity))
                else -> it.onNext(value)
            }
        } }

fun Payee.Companion.observablePayeeList(database: ObservableMoneyDatabase): Observable<List<Payee>> =
        database.onUpdate.flatMap { Observable.fromCallable { getAll(database) } }
