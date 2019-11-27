package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Single

fun Payee.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Payee> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Payee::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Payee.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }
