package com.munzenberger.money.app

import com.munzenberger.money.app.database.FileDatabaseConnector
import com.munzenberger.money.app.database.NewFileDatabaseConnector
import com.munzenberger.money.app.database.OpenFileDatabaseConnector
import javafx.application.Platform
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Window
import java.io.File

class ApplicationViewModel {

    companion object {
        const val DEFAULT_TITLE = "Money"
    }

    private val title = SimpleStringProperty(DEFAULT_TITLE)
    private val isConnectionInProgress = SimpleBooleanProperty(false)

    val titleProperty: ReadOnlyStringProperty = title
    val isConnectionInProgressProperty: ReadOnlyBooleanProperty = isConnectionInProgress

    init {
        MoneyApplication.observableDatabase.addListener { _, _, db ->
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
            it?.run { MoneyApplication.database = this }
        }
    }

    fun exit() {
        Platform.exit()
    }
}
