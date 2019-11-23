package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Single

fun Account.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Account> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Account::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Account.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }

fun Account.observableBalance(executor: QueryExecutor) = Single.fromCallable { balance(executor) }