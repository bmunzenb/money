package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor
import java.sql.Connection
import java.util.logging.Logger

/**
 * A connection to a Money database that allows storing and retrieving entities.
 * Retrieve an instance by calling [open].
 */
interface MoneyDatabase :
    QueryExecutor,
    AutoCloseable {
    /**
     * Name of this database for logging and display purposes
     */
    val name: String

    /**
     * Dialect of the database system
     */
    val dialect: DatabaseDialect

    companion object {
        private val logger = Logger.getLogger(MoneyDatabase::class.java.name)

        /**
         * Establishes a connection to a Money database.
         *
         * @param name Name of this database for logging and display purposes
         * @param dialect Dialect of the database system
         * @param connection the JDBC connection to the database
         *
         * @return an instance of `MoneyDatabase` that's backed by the JDBC connection. Callers should take care not to
         * execute queries in parallel.
         */
        fun open(
            name: String,
            dialect: DatabaseDialect,
            connection: Connection,
        ): MoneyDatabase =
            ConnectionMoneyDatabase(name, dialect, connection).apply {
                dialect.initialize(this)
                logger.info("Connected to money database: $name")
            }
    }
}
