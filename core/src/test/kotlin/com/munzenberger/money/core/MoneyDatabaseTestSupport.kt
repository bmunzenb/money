package com.munzenberger.money.core

import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.version.PendingUpgrades
import org.junit.After
import org.junit.Before
import java.sql.DriverManager

private data class DatabaseConfiguration(
        val driver: String,
        val url: String,
        val dialect: DatabaseDialect)

private val H2DatabaseConfiguration = DatabaseConfiguration(
        driver = "org.h2.Driver",
        url = "jdbc:h2:mem:",
        dialect = H2DatabaseDialect)

private val SQLiteDatabaseConfiguration = DatabaseConfiguration(
        driver = "org.sqlite.JDBC",
        url = "jdbc:sqlite::memory:",
        dialect = SQLiteDatabaseDialect)

open class MoneyDatabaseTestSupport {

    protected lateinit var database: MoneyDatabase

    @Before
    fun createDatabase() {

        val configuration = SQLiteDatabaseConfiguration

        Class.forName(configuration.driver)
        val connection = DriverManager.getConnection(configuration.url)

        database = ConnectionMoneyDatabase(configuration::class.java.simpleName, configuration.dialect, connection)

        val status = MoneyCoreVersionManager().getVersionStatus(database)
        when (status) {
            is PendingUpgrades -> status.apply()
            else -> {}
        }
    }

    @After
    fun closeConnection() {
        database.close()
    }
}
