package com.munzenberger.money.core

import com.munzenberger.money.core.version.MoneyDatabaseVersionManager
import com.munzenberger.money.version.VersionStatus
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import java.sql.DriverManager

private data class DatabaseConfiguration(
    val url: String,
    val dialect: DatabaseDialect,
)

private val H2DatabaseConfiguration =
    DatabaseConfiguration(
        url = "jdbc:h2:mem:",
        dialect = H2DatabaseDialect,
    )

private val SQLiteDatabaseConfiguration =
    DatabaseConfiguration(
        url = "jdbc:sqlite::memory:",
        dialect = SQLiteDatabaseDialect,
    )

open class MoneyDatabaseTestSupport {
    protected lateinit var database: MoneyDatabase

    @Before
    fun createDatabase() {
        val configuration = SQLiteDatabaseConfiguration

        val connection = DriverManager.getConnection(configuration.url)
        database = MoneyDatabase.open(configuration.url, configuration.dialect, connection)

        when (val status = MoneyDatabaseVersionManager().getVersionStatus(database)) {
            is VersionStatus.RequiresUpgrade -> status.applyUpgrade()
            is VersionStatus.UnsupportedVersion -> fail("Unsupported database version")
            else -> Unit // do nothing
        }
    }

    @After
    fun closeConnection() {
        database.close()
    }
}
