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
) : MoneyDatabase(name, dialect, ConnectionQueryExecutor(connection)) {

    private val logger = Logger.getLogger(ConnectionMoneyDatabase::class.java.simpleName)

    init {
        logger.info("opened money database: $name")
    }

    override fun close() {
        try {
            connection.close()
            logger.info("closed money database: $name")
        } catch (e: SQLException) {
            logger.log(Level.WARNING, e) { "failed to close money database: $name" }
        }
    }
}
