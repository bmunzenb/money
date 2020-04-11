package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import io.reactivex.Observable

fun Account.observableBalance(database: ObservableMoneyDatabase): Observable<Money> =
        database.onUpdate.flatMap { Observable.fromCallable { balance(database) } }
