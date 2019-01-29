package com.munzenberger.money.sql

import java.sql.Connection

class ConnectionQueryExecutor(private val connection: Connection) : QueryExecutor {

    override fun execute(query: Query) {
        SQLExecutor.execute(connection, query.sql, query.parameters)
    }

    override fun executeQuery(query: Query, handler: ResultSetHandler?) {
        SQLExecutor.executeQuery(connection, query.sql, query.parameters, handler)
    }

    override fun executeUpdate(query: Query, handler: ResultSetHandler?) {
        SQLExecutor.executeUpdate(connection, query.sql, query.parameters, handler)
    }
}
