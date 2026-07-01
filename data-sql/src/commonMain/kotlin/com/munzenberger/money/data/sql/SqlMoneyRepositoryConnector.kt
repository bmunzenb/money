package com.munzenberger.money.data.sql

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import java.io.File
import java.util.Properties

class SqlMoneyRepositoryConnector(private val url: String) : MoneyRepositoryConnector {

    constructor(file: File) : this("jdbc:sqlite:${file.absolutePath}")

    private val driver by lazy {
        JdbcSqliteDriver(
            url = url,
            properties = Properties().apply { put("foreign_keys", "true") },
        )
    }

    override suspend fun create(): MoneyRepositoryConnectionStatus {
        return try {
            MoneyDatabase.Schema.create(driver)
            MoneyRepositoryConnectionStatus.Ready(SqlMoneyRepository(driver))
        } catch (e: Exception) {
            MoneyRepositoryConnectionStatus.Failed(e)
        }
    }

    override suspend fun connect(): MoneyRepositoryConnectionStatus {
        return try {
            MoneyRepositoryConnectionStatus.Ready(SqlMoneyRepository(driver))
        } catch (e: Exception) {
            MoneyRepositoryConnectionStatus.Failed(e)
        }
    }
}
