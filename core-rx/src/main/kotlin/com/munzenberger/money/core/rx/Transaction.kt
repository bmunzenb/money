package com.munzenberger.money.core.rx

import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Single

fun Transaction.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Transaction> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Transaction::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Transaction.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }