package com.munzenberger.money.sql

import java.sql.Connection

abstract class DatabaseQueryExecutor : QueryExecutor {

    protected abstract fun getConnection(): Connection

    private fun execute(block: (Connection) -> Unit) {
        getConnection().use {
            block.invoke(it)
        }
    }

    override fun execute(query: Query) = execute {
        ConnectionQueryExecutor(it).execute(query)
    }

    override fun executeQuery(query: Query, handler: ResultSetHandler?) = execute {
        ConnectionQueryExecutor(it).executeQuery(query, handler)
    }

    override fun executeUpdate(query: Query, handler: ResultSetHandler?) = execute {
        ConnectionQueryExecutor(it).executeUpdate(query, handler)
    }
}
