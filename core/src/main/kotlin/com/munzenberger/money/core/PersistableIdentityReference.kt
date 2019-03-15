package com.munzenberger.money.core

import io.reactivex.Completable

internal class PersistableIdentityReference {

    private var value: Persistable<*>? = null
    private var updated = false

    fun set(value: Persistable<*>) {
        this.value = value
        this.updated = true
    }

    fun getIdentity(block: (Long?) -> Unit): Completable = when {
        updated -> Persistable.getIdentity(value, block)
        else -> Completable.complete()
    }
}
