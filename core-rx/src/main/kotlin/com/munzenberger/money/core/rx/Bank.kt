package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.PersistableNotFoundException
import io.reactivex.Observable

fun Bank.Companion.observableBank(identity: Long, database: ObservableMoneyDatabase): Observable<Bank> =
        database.onUpdate.flatMap { Observable.create<Bank> {
            when (val value = get(identity, database)) {
                null -> it.onError(PersistableNotFoundException(Bank::class, identity))
                else -> it.onNext(value)
            }
        } }

fun Bank.Companion.observableBankList(database: ObservableMoneyDatabase): Observable<List<Bank>> =
        database.onUpdate.flatMap { Observable.fromCallable { getAll(database) } }
