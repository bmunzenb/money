package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Single

fun Bank.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Bank> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Bank::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Bank.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }
