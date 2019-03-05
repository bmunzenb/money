package com.munzenberger.money.app.database

import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

class NewFileDatabaseConnector(private val ownerWindow: Window) {

    interface Callback : DatabaseConnector.Callback {
        override fun onConnectPendingUpgrades() = true
    }

    fun connect(callback: Callback) {

        val file: File? = FileChooser().apply {
            title = "New Money Database"
            initialDirectory = File(System.getProperty("user.home"))
            initialFileName = "Money"
        }.showSaveDialog(ownerWindow)

        file?.run {

            if (exists()) {
                delete()
            }

            FileDatabaseConnector().connect(this, callback)
        }
    }
}
