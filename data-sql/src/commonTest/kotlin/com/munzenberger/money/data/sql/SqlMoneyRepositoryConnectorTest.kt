package com.munzenberger.money.data.sql

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import kotlinx.coroutines.test.runTest
import java.io.File
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs

class SqlMoneyRepositoryConnectorTest {

    private val tempFiles = mutableListOf<File>()

    @AfterTest
    fun cleanup() {
        tempFiles.forEach { it.delete() }
    }

    private fun createTempFile(): File =
        File.createTempFile("money-test", ".db").also {
            it.deleteOnExit()
            tempFiles.add(it)
        }

    private fun openDriverOnFile(file: File): JdbcSqliteDriver =
        JdbcSqliteDriver(
            url = "jdbc:sqlite:${file.absolutePath}",
            properties = Properties().apply { put("foreign_keys", "true") },
        )

    @Test
    fun `connect returns Ready for new database`() = runTest {
        val connector = SqlMoneyRepositoryConnector(JdbcSqliteDriver.IN_MEMORY)
        val status = connector.connect()
        assertIs<MoneyRepositoryConnectionStatus.Ready>(status)
    }

    @Test
    fun `connect returns Ready for existing database at current schema version`() = runTest {
        val file = createTempFile()
        openDriverOnFile(file).use { driver ->
            MoneyDatabase.Schema.create(driver)
            driver.execute(null, "PRAGMA user_version = ${MoneyDatabase.Schema.version}", 0)
        }

        val status = SqlMoneyRepositoryConnector(file).connect()
        assertIs<MoneyRepositoryConnectionStatus.Ready>(status)
    }

    @Test
    fun `connect returns UnsupportedVersion for database with newer schema version`() = runTest {
        val file = createTempFile()
        openDriverOnFile(file).use { driver ->
            driver.execute(null, "PRAGMA user_version = ${MoneyDatabase.Schema.version + 1}", 0)
        }

        val status = SqlMoneyRepositoryConnector(file).connect()
        assertIs<MoneyRepositoryConnectionStatus.UnsupportedVersion>(status)
    }

    @Test
    fun `connect returns Failed for inaccessible database path`() = runTest {
        val connector = SqlMoneyRepositoryConnector(File("/nonexistent-directory/money-test.db"))
        val status = connector.connect()
        assertIs<MoneyRepositoryConnectionStatus.Failed>(status)
    }

    // TODO needs tests for migrations once at least one migration is available
}
