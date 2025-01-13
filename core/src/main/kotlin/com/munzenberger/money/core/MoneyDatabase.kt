package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor
import java.sql.DriverManager
import java.util.logging.Logger

/**
 * A connection to a Money database that allows storing and retrieving entities.
 * Retrieve an instance by calling [connect].
 */
interface MoneyDatabase : QueryExecutor {
    /**
     * Name of this database for logging and display purposes
     */
    val name: String

    /**
     * Dialect of the database system
     */
    val dialect: DatabaseDialect

    /**
     * Closes the connection to the database
     */
    fun close()

    companion object {
        private val logger = Logger.getLogger(MoneyDatabase::class.java.name)

        /**
         * Connect to a Money database specified by a JDB connection URL.
         *
         * @param name Name of this database for logging and display purposes
         * @param dialect Dialect of the database system
         * @param url JDBC connection URL
         * @param user Optional username
         * @param password Optional password
         *
         * @return an instance of `MoneyDatabase` that's backed by a single JDBC connection. Callers should take care
         * not to execute queries in parallel.
         */
        fun connect(
            name: String,
            dialect: DatabaseDialect,
            url: String,
            user: String? = null,
            password: String? = null,
        ): MoneyDatabase {
            val connection = DriverManager.getConnection(url, user, password)
            return ConnectionMoneyDatabase(name, dialect, connection).apply {
                dialect.initialize(this)
                logger.info("Connected to money database: $name")
            }
        }
    }
}
