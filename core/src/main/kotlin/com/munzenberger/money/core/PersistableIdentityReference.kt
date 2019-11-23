package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

internal class PersistableIdentityReference {

    private var value: Persistable<*>? = null
    private var updated = false

    fun set(value: Persistable<*>) {
        this.value = value
        this.updated = true
    }

    fun getIdentity(executor: QueryExecutor, block: (Long?) -> Unit) {
        if (updated) {
            val identity = value.getIdentity(executor)
            block.invoke(identity)
        }
    }
}
