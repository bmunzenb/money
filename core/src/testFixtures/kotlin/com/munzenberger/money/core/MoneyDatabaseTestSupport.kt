package com.munzenberger.money.core

import com.munzenberger.money.core.version.MoneyDatabaseVersionManager
import com.munzenberger.money.version.VersionStatus
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before

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

        database = MoneyDatabase.connect(configuration.url, configuration.dialect, configuration.url)

        when (val status = MoneyDatabaseVersionManager().getVersionStatus(database)) {
            is VersionStatus.PendingUpgrades -> status.apply()
            is VersionStatus.UnsupportedVersion -> fail("Unsupported database version")
            else -> Unit // do nothing
        }
    }

    @After
    fun closeConnection() {
        database.close()
    }
}
