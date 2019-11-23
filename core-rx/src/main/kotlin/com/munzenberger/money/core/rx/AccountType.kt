package com.munzenberger.money.core.rx

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Single

fun AccountType.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<AccountType> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(AccountType::class, identity))
        else -> it.onSuccess(value)
    }
}

fun AccountType.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }