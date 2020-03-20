package com.munzenberger.money.app.database

import com.munzenberger.money.app.ErrorAlert
import com.munzenberger.money.core.SQLiteDatabaseDialect

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
