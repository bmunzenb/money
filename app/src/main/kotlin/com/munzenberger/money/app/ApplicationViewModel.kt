package com.munzenberger.money.app

import com.munzenberger.money.app.database.DatabaseConnector
import com.munzenberger.money.app.database.DatabaseConnectorCallbacks
import com.munzenberger.money.app.database.FileDatabaseConnector
import com.munzenberger.money.app.database.MemoryDatabaseConnector
import com.munzenberger.money.app.database.ObservableMoneyDatabase
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
        connectToDatabase(FileDatabaseConnector(file), callbacks)
    }

    fun startMemoryDatabase(callbacks: DatabaseConnectorCallbacks) {
        connectToDatabase(MemoryDatabaseConnector(), callbacks)
    }

    private fun connectToDatabase(connector: DatabaseConnector, callbacks: DatabaseConnectorCallbacks, ) {

        isConnectionInProgress.bind(connector.isConnectionInProgressProperty)

        val callbacksWrapper = object : DatabaseConnectorCallbacks by callbacks {
            override fun onConnected(database: ObservableMoneyDatabase, isFirstUse: Boolean) {
                callbacks.onConnected(database, isFirstUse)
                connectedDatabase.value?.close()
                connectedDatabase.value = database
            }
        }

        connector.connect(callbacksWrapper)
    }

    override fun close() {
        connectedDatabase.value?.close()
    }
}
