package com.munzenberger.money.data.sql

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import kotlinx.coroutines.test.runTest
import java.io.File
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

    @Test
    fun `create returns Ready for in-memory database`() = runTest {
        val connector = SqlMoneyRepositoryConnector(JdbcSqliteDriver.IN_MEMORY)
        val status = connector.create()
        assertIs<MoneyRepositoryConnectionStatus.Ready>(status)
    }

    @Test
    fun `create returns Ready for file-based database`() = runTest {
        val connector = SqlMoneyRepositoryConnector(createTempFile())
        val status = connector.create()
        assertIs<MoneyRepositoryConnectionStatus.Ready>(status)
    }

    @Test
    fun `create returns Failed for inaccessible database path`() = runTest {
        val connector = SqlMoneyRepositoryConnector(File("/nonexistent-directory/money-test.db"))
        val status = connector.create()
        assertIs<MoneyRepositoryConnectionStatus.Failed>(status)
    }

    @Test
    fun `connect returns Ready for in-memory database`() = runTest {
        val connector = SqlMoneyRepositoryConnector(JdbcSqliteDriver.IN_MEMORY)
        val status = connector.connect()
        assertIs<MoneyRepositoryConnectionStatus.Ready>(status)
    }

    @Test
    fun `connect returns Ready for file-based database`() = runTest {
        val connector = SqlMoneyRepositoryConnector(createTempFile())
        val status = connector.connect()
        assertIs<MoneyRepositoryConnectionStatus.Ready>(status)
    }
}
