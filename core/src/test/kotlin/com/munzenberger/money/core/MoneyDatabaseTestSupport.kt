package com.munzenberger.money.core

import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.sql.ConnectionQueryExecutor
import com.munzenberger.money.version.PendingUpgrades
import org.junit.After
import org.junit.Before
import java.sql.Connection
import java.sql.DriverManager

open class MoneyDatabaseTestSupport {

    private lateinit var connection: Connection
    protected lateinit var database: MoneyDatabase

    @Before
    fun createDatabase() {

        Class.forName("org.h2.Driver")
        connection = DriverManager.getConnection("jdbc:h2:mem:")

        val executor = ConnectionQueryExecutor(connection)
        database = MoneyDatabase(executor)

        val status = MoneyCoreVersionManager().getVersionStatus(database)
        when (status) {
            is PendingUpgrades -> status.apply()
        }
    }

    @After
    fun closeConnection() {
        connection.close()
    }
}
