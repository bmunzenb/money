package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.PersistableNotFoundException
import io.reactivex.Observable

fun Account.Companion.observableAccount(identity: Long, database: ObservableMoneyDatabase): Observable<Account> =
        database.onUpdate.flatMap { Observable.create<Account> {
            when (val value = get(identity, database)) {
                null -> it.onError(PersistableNotFoundException(Account::class, identity))
                else -> it.onNext(value)
            }
        } }

fun Account.Companion.observableAccountList(database: ObservableMoneyDatabase): Observable<List<Account>> =
        database.onUpdate.flatMap { Observable.fromCallable { getAll(database) } }

fun Account.observableBalance(database: ObservableMoneyDatabase): Observable<Money> =
        database.onUpdate.flatMap { Observable.fromCallable { balance(database) } }
