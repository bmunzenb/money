package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

internal class PersistableIdentityReference(private var identity: Long?) {

    private var value: Persistable? = null
    private var dirty = false

    fun set(value: Persistable?) {
        this.value = value
        this.dirty = true
    }

    fun getIdentity(executor: QueryExecutor): Long? {
        if (dirty) {
            identity = value.getIdentity(executor)
            dirty = false
        }
        return identity
    }
}
