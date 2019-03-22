package com.munzenberger.money.app.database

import com.munzenberger.money.app.ErrorAlert
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.SQLiteDatabaseDialect
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.sql.Query
import javafx.scene.control.Alert
import java.lang.IllegalStateException

object MemoryDatabaseConnector : DatabaseConnector() {

    fun connect(complete: DatabaseConnectionHandler) {

        val driver = "org.sqlite.JDBC"
        val connectionUrl = "jdbc:sqlite::memory:"

        connect(name = connectionUrl,
                driver = driver,
                connectionUrl = connectionUrl,
                dialect = SQLiteDatabaseDialect,
                complete = complete)
    }

    override fun onUnsupportedVersion() {
        throw IllegalStateException("unsupported database version")
    }

    override fun onPendingUpgrades() = true

    override fun onConnectError(error: Throwable) {

        ErrorAlert.showAndWait(error)
    }
}
