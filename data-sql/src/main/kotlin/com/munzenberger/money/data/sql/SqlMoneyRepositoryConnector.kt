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

/** Connects to the SQLite database at [url], running its database work in [context]. */
sealed class SqlMoneyRepositoryConnector(
    protected val url: String,
    private val context: CoroutineContext,
) : MoneyRepositoryConnector {

    /** What connecting does to the database, for the failure log, e.g. "open". */
    protected abstract val action: String

    /** Builds a repository on [driver]. */
    protected abstract fun repository(driver: SqlDriver): SqlMoneyRepository

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

    /** Opens a driver and builds a repository on it, closing the driver if that fails. */
    override suspend fun connect(): MoneyRepositoryConnectionStatus = withContext(context) {
        var driver: SqlDriver? = null
        try {
            driver = openDriver()
            MoneyRepositoryConnectionStatus.Ready(repository(driver))
        } catch (e: CancellationException) {
            driver?.close()
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.log(Level.WARNING, "Failed to $action database: $url", e)
            driver?.close()
            MoneyRepositoryConnectionStatus.Failed(e)
        }
    }

    /** Creates the schema on [driver] and builds a repository on it. */
    protected fun createRepository(driver: SqlDriver): SqlMoneyRepository {
        val database = createMoneyDatabase(driver)
        // In one transaction, so a failure partway through doesn't leave a partial schema behind.
        database.transaction { MoneyDatabase.Schema.create(driver) }
        logger.info("Created database: $url")
        return SqlMoneyRepository(url, driver, database)
    }

    protected companion object {
        fun fileUrl(file: File): String = "jdbc:sqlite:${file.absolutePath}"
    }
}

/** Connects to the existing database in [file]. */
class ExistingFileSqlMoneyRepositoryConnector(
    file: File,
    context: CoroutineContext = Dispatchers.IO,
) : SqlMoneyRepositoryConnector(fileUrl(file), context) {

    override val action = "open"

    override fun repository(driver: SqlDriver): SqlMoneyRepository {
        val repository = SqlMoneyRepository(url, driver)
        logger.info("Opened database: $url")
        return repository
    }
}

/** Connects to a new database in [file], creating its schema. */
class NewFileSqlMoneyRepositoryConnector(
    file: File,
    context: CoroutineContext = Dispatchers.IO,
) : SqlMoneyRepositoryConnector(fileUrl(file), context) {

    override val action = "create"

    override fun repository(driver: SqlDriver): SqlMoneyRepository = createRepository(driver)
}

/** Connects to a new, empty in-memory database, which is discarded when it's closed. */
class InMemorySqlMoneyRepositoryConnector(
    context: CoroutineContext = Dispatchers.IO,
) : SqlMoneyRepositoryConnector(JdbcSqliteDriver.IN_MEMORY, context) {

    override val action = "create"

    override fun repository(driver: SqlDriver): SqlMoneyRepository = createRepository(driver)
}
