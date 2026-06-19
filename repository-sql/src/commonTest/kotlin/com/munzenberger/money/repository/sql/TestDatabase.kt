package com.munzenberger.money.repository.sql

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.logs.LogSqliteDriver
import java.util.Properties

fun createTestJdbcDriver() = LogSqliteDriver(
    sqlDriver = JdbcSqliteDriver(
        url = JdbcSqliteDriver.IN_MEMORY,
        properties = Properties().apply { put("foreign_keys", "true") }
    ),
    logger = { println(it) }
)

fun createTestDatabase(): MoneyDatabase {
    val driver = createTestJdbcDriver()
    MoneyDatabase.Schema.create(driver)
    return MoneyDatabase(driver)
}
