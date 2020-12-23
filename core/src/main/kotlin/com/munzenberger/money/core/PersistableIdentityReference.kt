package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

internal class PersistableIdentityReference(private var identity: Long?) {

    private var value: Persistable<*>? = null
    private var updated = false

    fun set(value: Persistable<*>?) {
        this.value = value
        this.updated = true
    }

    fun getIdentity(executor: QueryExecutor): Long? {
        if (updated) {
            identity = value.getIdentity(executor)
            updated = false
        }
        return identity
    }
}
