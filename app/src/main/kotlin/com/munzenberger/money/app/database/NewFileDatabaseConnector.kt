package com.munzenberger.money.app.database

import com.munzenberger.money.core.MoneyDatabase
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

object NewFileDatabaseConnector {

    fun connect(ownerWindow: Window, success: (MoneyDatabase) -> Unit) {

        val file: File? = FileChooser().apply {
            title = "New Money Database"
            initialDirectory = File(System.getProperty("user.home"))
            initialFileName = "Money${FileDatabaseConnector.SUFFIX}"
        }.showSaveDialog(ownerWindow)

        file?.run {

            if (exists()) {
                delete()
            }

            FileDatabaseConnector.connect(this, object : DatabaseConnector.Callback {

                override fun onConnectSuccess(database: MoneyDatabase) {
                    success.invoke(database)
                }

                override fun onConnectPendingUpgrades() = true

                override fun onConnectUnsupportedVersion() {
                    // this should not happen when creating a new file
                    throw IllegalStateException("unsupported database version")
                }

                override fun onConnectError(error: Throwable) {
                    Alert(Alert.AlertType.ERROR).apply {
                        title = "Error"
                        headerText = "Could not create database file"
                        contentText = error.message
                    }.showAndWait()
                }
            })
        }
    }
}
