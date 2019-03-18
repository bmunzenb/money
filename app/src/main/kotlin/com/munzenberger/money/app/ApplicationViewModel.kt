package com.munzenberger.money.app

import com.munzenberger.money.app.database.FileDatabaseConnector
import com.munzenberger.money.app.database.NewFileDatabaseConnector
import com.munzenberger.money.app.database.OpenFileDatabaseConnector
import com.munzenberger.money.core.MoneyDatabase
import javafx.application.Platform
import javafx.beans.property.*
import javafx.stage.Window
import java.io.File

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
        NewFileDatabaseConnector.openFile(ownerWindow)?.run {
            connectToFileDatabase(NewFileDatabaseConnector, this)
        }
    }

    fun openDatabase(ownerWindow: Window) {
        OpenFileDatabaseConnector.openFile(ownerWindow)?.run {
            connectToFileDatabase(OpenFileDatabaseConnector, this)
        }
    }

    private fun connectToFileDatabase(connector: FileDatabaseConnector, file: File) {
        isConnectionInProgress.value = true
        connector.connect(file) {
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
