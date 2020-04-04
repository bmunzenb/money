package com.munzenberger.money.app

import com.munzenberger.money.app.database.DatabaseConnectionHandler
import com.munzenberger.money.app.database.MemoryDatabaseConnector
import com.munzenberger.money.app.database.NewFileDatabaseConnector
import com.munzenberger.money.app.database.OpenFileDatabaseConnector
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import javafx.application.Platform
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Window

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
            title.value = db?.name ?: DEFAULT_TITLE
        }
    }

    fun createDatabase(ownerWindow: Window) {
        connectToDatabase {
            NewFileDatabaseConnector(ownerWindow).connect(it)
        }
    }

    fun openDatabase(ownerWindow: Window) {
        connectToDatabase {
            OpenFileDatabaseConnector(ownerWindow).connect(it)
        }

    }

    fun startMemoryDatabase() {
        connectToDatabase {
            MemoryDatabaseConnector.connect(it)
        }
    }

    private fun connectToDatabase(block: (DatabaseConnectionHandler) -> Unit) {
        isConnectionInProgress.value = true
        block.invoke {
            isConnectionInProgress.value = false
            it?.run {
                connectedDatabase.value?.close()
                connectedDatabase.value = it
            }
        }
    }

    fun exit() {
        Platform.exit()
    }

    override fun close() {
        connectedDatabase.value?.close()
    }
}
