package com.munzenberger.money.core

import com.munzenberger.money.sql.ConnectionQueryExecutor
import java.io.IOException
import java.sql.Connection
import java.util.logging.Level
import java.util.logging.Logger

class MoneyDatabase(private val connection: Connection) : ConnectionQueryExecutor(connection) {

    private val logger = Logger.getLogger(MoneyDatabase::class.java.simpleName)

    val name = connection.toString()

    fun close() = try {
        connection.close()
        logger.log(Level.INFO, "closed money database: $connection")
    } catch (e: IOException) {
        logger.log(Level.WARNING, e) { "could not close database connection" }
    }
}
