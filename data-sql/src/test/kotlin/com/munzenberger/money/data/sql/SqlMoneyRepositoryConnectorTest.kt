package com.munzenberger.money.data.sql

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import kotlinx.coroutines.test.runTest
import java.io.File
import java.sql.DriverManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `create leaves no partial schema when it fails partway`() = runTest {
        val file = createTempFile()
        // A table the schema creates last fails its CREATE TABLE after the others have run.
        execute(file, "CREATE TABLE TransactionStatus (id INTEGER)")

        val status = SqlMoneyRepositoryConnector(file).create()

        assertIs<MoneyRepositoryConnectionStatus.Failed>(status)
        assertEquals(listOf("TransactionStatus"), tableNames(file))
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

    private fun execute(file: File, sql: String) {
        DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { connection ->
            connection.createStatement().use { statement -> statement.execute(sql) }
        }
    }

    private fun tableNames(file: File): List<String> =
        DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table'").use { rows ->
                    buildList { while (rows.next()) add(rows.getString(1)) }
                }
            }
        }
}
