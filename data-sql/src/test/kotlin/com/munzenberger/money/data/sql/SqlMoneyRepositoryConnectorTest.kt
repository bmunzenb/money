package com.munzenberger.money.data.sql

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
    fun `new file connector returns Ready for file-based database`() = runTest {
        val connector = NewFileSqlMoneyRepositoryConnector(createTempFile())
        val status = connector.connect()
        assertIs<MoneyRepositoryConnectionStatus.Ready>(status)
    }

    @Test
    fun `new file connector returns Failed for inaccessible database path`() = runTest {
        val connector = NewFileSqlMoneyRepositoryConnector(File("/nonexistent-directory/money-test.db"))
        val status = connector.connect()
        assertIs<MoneyRepositoryConnectionStatus.Failed>(status)
    }

    @Test
    fun `new file connector leaves no partial schema when it fails partway`() = runTest {
        val file = createTempFile()
        // A table the schema creates last fails its CREATE TABLE after the others have run.
        execute(file, "CREATE TABLE TransactionStatus (id INTEGER)")

        val status = NewFileSqlMoneyRepositoryConnector(file).connect()

        assertIs<MoneyRepositoryConnectionStatus.Failed>(status)
        assertEquals(listOf("TransactionStatus"), tableNames(file))
    }

    @Test
    fun `existing file connector returns Ready for file-based database`() = runTest {
        val file = createTempFile()
        val created = NewFileSqlMoneyRepositoryConnector(file).connect()
        check(created is MoneyRepositoryConnectionStatus.Ready)
        created.moneyRepository.close()

        val status = ExistingFileSqlMoneyRepositoryConnector(file).connect()

        assertIs<MoneyRepositoryConnectionStatus.Ready>(status)
    }

    @Test
    fun `in-memory connector returns Ready`() = runTest {
        val connector = InMemorySqlMoneyRepositoryConnector()
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
