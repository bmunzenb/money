package com.munzenberger.money.core

import com.munzenberger.money.sql.ConnectionQueryExecutor
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger

class ConnectionMoneyDatabase(
        name: String,
        dialect: DatabaseDialect,
        private val connection: Connection
) : AbstractMoneyDatabase(name, dialect, ConnectionQueryExecutor(connection)) {

    private val logger = Logger.getLogger(MoneyDatabase::class.java.simpleName)

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
}
