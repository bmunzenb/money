package com.munzenberger.money.app

import com.munzenberger.money.app.database.DatabaseConnector
import com.munzenberger.money.app.database.DatabaseConnectorCallbacks
import com.munzenberger.money.app.database.FileDatabaseConnector
import com.munzenberger.money.app.database.MemoryDatabaseConnector
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import javafx.application.Platform
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.io.File

class ApplicationViewModel : AutoCloseable {

    companion object {
        const val DEFAULT_TITLE = "Money"
    }

    private val title = SimpleStringProperty(DEFAULT_TITLE)
    private val isConnectionInProgress = SimpleBooleanProperty(false)
    private val connectedDatabase = SimpleObjectProperty<ObservableMoneyDatabase?>()

    val titleProperty: ReadOnlyStringProperty = title
    val isConnectionInProgressProperty: ReadOnlyBooleanProperty = isConnectionInProgress
    val connectedDatabaseProperty: ReadOnlyObjectProperty<ObservableMoneyDatabase?> = connectedDatabase

    init {
        connectedDatabaseProperty.addListener { _, _, db ->
            title.value = when (db) {
                null -> DEFAULT_TITLE
                else -> "${db.name} - $DEFAULT_TITLE"
            }
        }
    }

    fun openFileDatabase(file: File, callbacks: DatabaseConnectorCallbacks) {
        connectToDatabase(callbacks, FileDatabaseConnector(file))
    }

    fun startMemoryDatabase(callbacks: DatabaseConnectorCallbacks) {
        connectToDatabase(callbacks, MemoryDatabaseConnector())
    }

    private fun connectToDatabase(callbacks: DatabaseConnectorCallbacks, connector: DatabaseConnector) {

        isConnectionInProgress.value = true

        val callbacksWrapper = object : DatabaseConnectorCallbacks {

            override fun onCanceled() {
                callbacks.onCanceled()
                isConnectionInProgress.value = false
            }

            override fun onConnected(database: ObservableMoneyDatabase) {
                callbacks.onConnected(database)
                isConnectionInProgress.value = false
                connectedDatabase.value?.close()
                connectedDatabase.value = database
            }

            override fun onConnectError(error: Throwable) {
                callbacks.onConnectError(error)
                isConnectionInProgress.value = false
            }

            override fun onUnsupportedVersion() {
                callbacks.onUnsupportedVersion()
                isConnectionInProgress.value = false
            }

            override fun onPendingUpgrades(): Boolean {
                return callbacks.onPendingUpgrades()
            }
        }

        connector.connect(callbacksWrapper)
    }

    fun exit() {
        Platform.exit()
    }

    override fun close() {
        connectedDatabase.value?.close()
    }
}
