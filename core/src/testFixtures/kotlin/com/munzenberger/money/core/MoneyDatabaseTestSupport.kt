package com.munzenberger.money.core

import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.version.VersionStatus
import org.junit.After
import org.junit.Before
import java.sql.DriverManager

private data class DatabaseConfiguration(
        val url: String,
        val dialect: DatabaseDialect)

private val H2DatabaseConfiguration = DatabaseConfiguration(
        url = "jdbc:h2:mem:",
        dialect = H2DatabaseDialect)

private val SQLiteDatabaseConfiguration = DatabaseConfiguration(
        url = "jdbc:sqlite::memory:",
        dialect = SQLiteDatabaseDialect)

open class MoneyDatabaseTestSupport {

    protected lateinit var database: MoneyDatabase

    @Before
    fun createDatabase() {

        val configuration = SQLiteDatabaseConfiguration

        val connection = DriverManager.getConnection(configuration.url)

        database = ConnectionMoneyDatabase(configuration.url, configuration.dialect, connection).also {
            configuration.dialect.initialize(it)
        }

        when (val status = MoneyCoreVersionManager().getVersionStatus(database)) {
            is VersionStatus.PendingUpgrades -> status.apply()
            else -> Unit // do nothing
        }
    }

    @After
    fun closeConnection() {
        database.close()
    }
}
