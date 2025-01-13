package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

interface MoneyEntity<I : Identity> {
    val identity: I?

    fun save(executor: QueryExecutor)

    fun delete(executor: QueryExecutor)
}

// TODO reconsider auto-saving entities
internal fun <I : Identity> MoneyEntity<I>.getAutoSavedIdentity(executor: QueryExecutor) =
    when {
        identity == null -> {
            save(executor)
            identity
        }

        else ->
            identity
    }
