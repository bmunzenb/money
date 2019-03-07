package com.munzenberger.money.core

import com.munzenberger.money.sql.ConnectionQueryExecutor
import com.munzenberger.money.sql.Query
import java.io.IOException
import java.sql.Connection
import java.util.logging.Level
import java.util.logging.Logger

class MoneyDatabase(
        private val connection: Connection,
        val dialect: DatabaseDialect,
        name: String? = null
) : ConnectionQueryExecutor(connection) {

    private val logger = Logger.getLogger(MoneyDatabase::class.java.simpleName)

    val name = name ?: connection.toString()

    init {

        when (dialect) {
            // SQLite requires enabling foreign keys
            // https://www.sqlite.org/foreignkeys.html#fk_enable
            is SQLiteDatabaseDialect -> execute(Query("PRAGMA foreign_keys = ON"))
        }

        logger.info("opened money database: $name")
    }

    fun close() = try {
        connection.close()
        logger.log(Level.INFO, "closed money database: $name")
    } catch (e: IOException) {
        logger.log(Level.WARNING, e) { "could not close database connection" }
    }
}
