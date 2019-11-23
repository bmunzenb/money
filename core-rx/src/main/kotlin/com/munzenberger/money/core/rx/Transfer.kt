package com.munzenberger.money.core.rx

import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transfer
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Single

fun Transfer.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Transfer> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Transfer::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Transfer.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }