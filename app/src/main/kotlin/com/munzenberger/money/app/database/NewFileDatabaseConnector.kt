package com.munzenberger.money.app.database

import javafx.scene.control.Alert
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

object NewFileDatabaseConnector : FileDatabaseConnector() {

    override fun openFile(ownerWindow: Window): File? = FileChooser().apply {
        title = "New Money Database"
        initialDirectory = File(System.getProperty("user.home"))
        initialFileName = "Money${FileDatabaseConnector.SUFFIX}"
        extensionFilters.addAll(
                FileChooser.ExtensionFilter("Money Database Files", "*${FileDatabaseConnector.SUFFIX}"),
                FileChooser.ExtensionFilter("All Files", "*"))
    }.showSaveDialog(ownerWindow)

    override fun connect(file: File, complete: DatabaseConnectionHandler) {

        if (file.exists()) {
            if (!file.delete()) {

                Alert(Alert.AlertType.ERROR).apply {
                    title = "Error"
                    contentText = "Could not delete existing file."
                }.showAndWait()

                return
            }
        }

        super.connect(file, complete)
    }

    override fun onUnsupportedVersion() {
        // this should not happen when creating a new file
        throw IllegalStateException("unsupported database version")
    }

    override fun onPendingUpgrades() = true

    override fun onConnectError(error: Throwable) {

        Alert(Alert.AlertType.ERROR).apply {
            title = "Error"
            headerText = "Could not create database file"
            contentText = error.message
        }.showAndWait()
    }
}
