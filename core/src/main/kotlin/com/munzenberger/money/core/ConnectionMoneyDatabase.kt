package com.munzenberger.money.core

import com.munzenberger.money.sql.ConnectionQueryExecutor
import com.munzenberger.money.sql.QueryExecutor
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger

internal class ConnectionMoneyDatabase(
    override val name: String,
    override val dialect: DatabaseDialect,
    private val connection: Connection,
) : MoneyDatabase, QueryExecutor by ConnectionQueryExecutor(connection) {
    private val logger = Logger.getLogger(MoneyDatabase::class.java.name)

    override fun close() {
        try {
            connection.close()
            logger.info("Closed connection to money database: $name")
        } catch (e: SQLException) {
            logger.log(Level.WARNING, "Failed to close connection to money database: $name", e)
        }
    }
}
