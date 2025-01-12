package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

internal class IdentityReference<I : Identity>(private var _identity: I?) {
    private var value: MoneyEntity<I>? = null
    private var dirty = false

    fun set(value: MoneyEntity<I>?) {
        this.value = value
        this.dirty = true
    }

    val identity: I?
        get() =
            when (dirty) {
                true -> value?.identity
                else -> _identity
            }

    fun getAutoSavedIdentity(executor: QueryExecutor): I? {
        if (dirty) {
            _identity = value?.getAutoSavedIdentity(executor)
            dirty = false
        }
        return _identity
    }
}
