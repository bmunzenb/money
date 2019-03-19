package com.munzenberger.money.app

import com.munzenberger.money.app.database.DatabaseConnectionHandler
import com.munzenberger.money.app.database.MemoryDatabaseConnector
import com.munzenberger.money.app.database.NewFileDatabaseConnector
import com.munzenberger.money.app.database.OpenFileDatabaseConnector
import com.munzenberger.money.core.MoneyDatabase
import javafx.application.Platform
import javafx.beans.property.*
import javafx.stage.Window

class ApplicationViewModel {

    companion object {
        const val DEFAULT_TITLE = "Money"
    }

    private val title = SimpleStringProperty(DEFAULT_TITLE)
    private val isConnectionInProgress = SimpleBooleanProperty(false)
    private val connectedDatabase = SimpleObjectProperty<MoneyDatabase?>()

    val titleProperty: ReadOnlyStringProperty = title
    val isConnectionInProgressProperty: ReadOnlyBooleanProperty = isConnectionInProgress
    val connectedDatabaseProperty: ReadOnlyObjectProperty<MoneyDatabase?> = connectedDatabase

    init {
        connectedDatabaseProperty.addListener { _, _, db ->
            title.value = db?.name ?: DEFAULT_TITLE
        }
    }

    fun createDatabase(ownerWindow: Window) {
        NewFileDatabaseConnector.openFile(ownerWindow)?.let { file ->
            connectToDatabase {
                NewFileDatabaseConnector.connect(file, it)
            }
        }
    }

    fun openDatabase(ownerWindow: Window) {
        OpenFileDatabaseConnector.openFile(ownerWindow)?.let { file ->
            connectToDatabase {
                OpenFileDatabaseConnector.connect(file, it)
            }
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

    fun shutdown() {
        connectedDatabase.value?.close()
    }
}
