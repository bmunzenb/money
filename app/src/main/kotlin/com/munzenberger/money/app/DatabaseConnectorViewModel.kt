package com.munzenberger.money.app

import com.munzenberger.money.app.database.NewFileDatabaseConnector
import com.munzenberger.money.app.database.OpenFileDatabaseConnector
import javafx.stage.Window

interface DatabaseConnectorViewModel {

    fun createDatabase(ownerWindow: Window) {

        ProgressDialog.doInDialog(ownerWindow, "Creating new database...") { dialog ->
            NewFileDatabaseConnector.connect(ownerWindow) {
                dialog.close()
                it?.run { MoneyApplication.database = this }
            }
        }
    }

    fun openDatabase(ownerWindow: Window) {

        ProgressDialog.doInDialog(ownerWindow, "Opening database...") { dialog ->
            OpenFileDatabaseConnector.connect(ownerWindow) {
                dialog.close()
                it?.run { MoneyApplication.database = this }
            }
        }
    }
}
