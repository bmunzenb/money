package com.munzenberger.money.core

import com.munzenberger.money.core.version.MoneyCoreVersionManager
import com.munzenberger.money.version.PendingUpgrades
import org.junit.After
import org.junit.Before
import java.sql.DriverManager

open class MoneyDatabaseTestSupport {

    protected lateinit var database: MoneyDatabase

    @Before
    fun createDatabase() {

        Class.forName("org.h2.Driver")
        val connection = DriverManager.getConnection("jdbc:h2:mem:")

        database = MoneyDatabase(connection)

        val status = MoneyCoreVersionManager().getVersionStatus(database)
        when (status) {
            is PendingUpgrades -> status.apply()
        }
    }

    @After
    fun closeConnection() {
        database.close()
    }
}
