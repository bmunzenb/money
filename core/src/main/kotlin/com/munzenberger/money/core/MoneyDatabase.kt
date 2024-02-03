package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor
import java.sql.DriverManager
import java.util.logging.Logger

interface MoneyDatabase : QueryExecutor {

    val name: String

    val dialect: DatabaseDialect

    fun close()

    companion object {

        private val logger = Logger.getLogger(MoneyDatabase::class.simpleName)

        fun connect(
            name: String,
            dialect: DatabaseDialect,
            url: String,
            user: String? = null,
            password: String? = null
        ): MoneyDatabase {
            val connection = DriverManager.getConnection(url, user, password)
            return ConnectionMoneyDatabase(name, dialect, connection).apply {
                dialect.initialize(this)
                logger.info("Connected to money database: $name")
            }
        }
    }
}
