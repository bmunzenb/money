package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

internal class PersistableIdentityReference<I : Identity>(private var identity: I?) {

    private var value: MoneyEntity<I>? = null
    private var dirty = false

    fun set(value: MoneyEntity<I>?) {
        this.value = value
        this.dirty = true
    }

    fun getIdentity(executor: QueryExecutor): I? {
        if (dirty) {
            identity = value.getIdentity(executor)
            dirty = false
        }
        return identity
    }
}
