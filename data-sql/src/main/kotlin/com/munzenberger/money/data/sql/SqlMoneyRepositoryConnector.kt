package com.munzenberger.money.data.sql

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.logs.LogSqliteDriver
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

class SqlMoneyRepositoryConnector(
    private val url: String,
    private val context: CoroutineContext = Dispatchers.IO,
) : MoneyRepositoryConnector {

    constructor(
        file: File,
        context: CoroutineContext = Dispatchers.IO,
    ) : this("jdbc:sqlite:${file.absolutePath}", context)

    private fun openDriver(): SqlDriver =
        LogSqliteDriver(
            sqlDriver = JdbcSqliteDriver(
                url = url,
                properties = Properties().apply { put("foreign_keys", "true") },
            ),
            logger = {
                logger.log(Level.FINE, it)
            }
        )

    override suspend fun create(): MoneyRepositoryConnectionStatus = open(action = "create") { driver ->
        val database = createMoneyDatabase(driver)
        // In one transaction, so a failure partway through doesn't leave a partial schema behind.
        database.transaction { MoneyDatabase.Schema.create(driver) }
        logger.info("Created database: $url")
        SqlMoneyRepository(url, driver, database)
    }

    override suspend fun connect(): MoneyRepositoryConnectionStatus = open(action = "open") { driver ->
        val repository = SqlMoneyRepository(url, driver)
        logger.info("Opened database: $url")
        repository
    }

    /** Opens a driver and builds a repository on it with [block], closing the driver if that fails. */
    private suspend fun open(
        action: String,
        block: (SqlDriver) -> SqlMoneyRepository,
    ): MoneyRepositoryConnectionStatus = withContext(context) {
        var driver: SqlDriver? = null
        try {
            driver = openDriver()
            MoneyRepositoryConnectionStatus.Ready(block(driver))
        } catch (e: CancellationException) {
            driver?.close()
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.log(Level.WARNING, "Failed to $action database: $url", e)
            driver?.close()
            MoneyRepositoryConnectionStatus.Failed(e)
        }
    }

    companion object {
        /** Returns a connector to a new, empty in-memory database, which is discarded when it's closed. */
        fun inMemory(context: CoroutineContext = Dispatchers.IO): SqlMoneyRepositoryConnector =
            SqlMoneyRepositoryConnector(JdbcSqliteDriver.IN_MEMORY, context)
    }
}
