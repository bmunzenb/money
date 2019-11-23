package com.munzenberger.money.core.rx

import com.munzenberger.money.core.Category
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Single

fun Category.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Category> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Category::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Category.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }