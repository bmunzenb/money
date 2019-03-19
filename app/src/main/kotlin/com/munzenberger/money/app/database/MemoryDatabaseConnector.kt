package com.munzenberger.money.app.database

import com.munzenberger.money.core.SQLiteDatabaseDialect
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

        Alert(Alert.AlertType.ERROR).apply {
            title = "Error"
            headerText = "Could not start in-memory database."
            contentText = error.message
        }.showAndWait()
    }
}
