package com.munzenberger.money.data.sql

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.logs.LogSqliteDriver
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import java.io.File
import java.util.Properties
import java.util.logging.Level

class SqlMoneyRepositoryConnector(private val url: String) : MoneyRepositoryConnector {

    constructor(file: File) : this("jdbc:sqlite:${file.absolutePath}")

    private val driver by lazy {
        LogSqliteDriver(
            sqlDriver = JdbcSqliteDriver(
                url = url,
                properties = Properties().apply { put("foreign_keys", "true") },
            ),
            logger = {
                logger.log(Level.FINE, it)
            }
        )
    }

    override suspend fun create(): MoneyRepositoryConnectionStatus {
        return try {
            MoneyDatabase.Schema.create(driver)
            logger.info("Created database: $url")
            MoneyRepositoryConnectionStatus.Ready(SqlMoneyRepository(url, driver))
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to create database: $url", e)
            MoneyRepositoryConnectionStatus.Failed(e)
        }
    }

    override suspend fun connect(): MoneyRepositoryConnectionStatus {
        return try {
            val repository = SqlMoneyRepository(url, driver)
            logger.info("Opened database: $url")
            MoneyRepositoryConnectionStatus.Ready(repository)
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to open database: $url", e)
            MoneyRepositoryConnectionStatus.Failed(e)
        }
    }
}
