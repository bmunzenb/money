package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor
import io.reactivex.Completable

internal class PersistableIdentityReference {

    private var value: Persistable<*>? = null
    private var updated = false

    fun set(value: Persistable<*>) {
        this.value = value
        this.updated = true
    }

    fun getIdentity(executor: QueryExecutor, block: (Long?) -> Unit): Completable = when {
        updated -> value.getIdentity(executor, block)
        else -> Completable.complete()
    }
}
