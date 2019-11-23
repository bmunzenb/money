package com.munzenberger.money.core

import com.munzenberger.money.sql.ConnectionQueryExecutor
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger

class ConnectionMoneyDatabase(
        override val name: String,
        override val dialect: DatabaseDialect,
        private val connection: Connection
) : MoneyDatabase {

    private val executor = ConnectionQueryExecutor(connection)

    private val logger = Logger.getLogger(ConnectionMoneyDatabase::class.java.simpleName)

    init {
        logger.info("Connected to money database: $name")
    }

    override fun close() {
        try {
            connection.close()
            logger.info("Closed connection to money database: $name")
        } catch (e: SQLException) {
            logger.log(Level.WARNING, "Failed to close connection to money database: $name", e)
        }
    }

    override fun execute(query: Query) =
            executor.execute(query)

    override fun executeQuery(query: Query, handler: ResultSetHandler?) =
            executor.executeQuery(query, handler)

    override fun executeUpdate(query: Query, handler: ResultSetHandler?) =
            executor.executeUpdate(query, handler)

    override fun createTransaction() =
            executor.createTransaction()
}
