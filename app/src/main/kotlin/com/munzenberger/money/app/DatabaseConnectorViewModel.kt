package com.munzenberger.money.app

import com.munzenberger.money.app.database.NewFileDatabaseConnector
import com.munzenberger.money.app.database.OpenFileDatabaseConnector
import javafx.stage.Window

interface DatabaseConnectorViewModel {

    fun createDatabase(ownerWindow: Window) {
        NewFileDatabaseConnector.connect(ownerWindow) {
            MoneyApplication.database = it
        }
    }

    fun openDatabase(ownerWindow: Window) {
        OpenFileDatabaseConnector.connect(ownerWindow) {
            MoneyApplication.database = it
        }
    }
}
