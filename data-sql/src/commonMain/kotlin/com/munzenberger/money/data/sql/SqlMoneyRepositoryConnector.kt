package com.munzenberger.money.data.sql

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import java.io.File
import java.util.Properties

class SqlMoneyRepositoryConnector(private val url: String) : MoneyRepositoryConnector {

    constructor(file: File) : this("jdbc:sqlite:${file.absolutePath}")

    override suspend fun connect(): MoneyRepositoryConnectionStatus {
        return try {
            val driver = JdbcSqliteDriver(
                url = url,
                properties = Properties().apply { put("foreign_keys", "true") },
            )
            val existingVersion = driver.userVersion()
            val schemaVersion = MoneyDatabase.Schema.version

            when {
                existingVersion == 0L -> {
                    MoneyDatabase.Schema.create(driver)
                    MoneyRepositoryConnectionStatus.Ready(SqlMoneyRepository(MoneyDatabase(driver)))
                }
                existingVersion == schemaVersion -> {
                    MoneyRepositoryConnectionStatus.Ready(SqlMoneyRepository(MoneyDatabase(driver)))
                }
                existingVersion < schemaVersion -> {
                    SqlMigrationStatus(driver, existingVersion, schemaVersion)
                }
                else -> {
                    driver.close()
                    MoneyRepositoryConnectionStatus.UnsupportedVersion
                }
            }
        } catch (e: Exception) {
            MoneyRepositoryConnectionStatus.Failed(e)
        }
    }
}

private fun SqlDriver.userVersion(): Long =
    executeQuery(
        identifier = null,
        sql = "PRAGMA user_version",
        mapper = { cursor -> QueryResult.Value(if (cursor.next().value) cursor.getLong(0) ?: 0L else 0L) },
        parameters = 0,
    ).value

class SqlMigrationStatus(
    private val driver: SqlDriver,
    private val fromVersion: Long,
    private val toVersion: Long,
) : MoneyRepositoryConnectionStatus.RequiresMigration {
    override suspend fun migrate(): MoneyRepositoryConnectionStatus {
        return try {
            MoneyDatabase.Schema.migrate(driver, fromVersion, toVersion)
            MoneyRepositoryConnectionStatus.Ready(SqlMoneyRepository(MoneyDatabase(driver)))
        } catch (e: Exception) {
            MoneyRepositoryConnectionStatus.Failed(e)
        }
    }
}
