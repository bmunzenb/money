package com.munzenberger.money.app

import com.munzenberger.money.app.database.FileDatabaseConnector
import com.munzenberger.money.app.database.NewFileDatabaseConnector
import com.munzenberger.money.app.database.OpenFileDatabaseConnector
import javafx.stage.Window
import java.io.File

interface DatabaseConnectorViewModel {

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
        connector.connect(file) {
            it?.run { MoneyApplication.database = this }
        }
    }
}
