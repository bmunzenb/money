package com.munzenberger.money.app.database

import com.munzenberger.money.app.ErrorAlert
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

class NewFileDatabaseConnector(private val ownerWindow: Window) : FileDatabaseConnector() {

    override fun openFile(): File? {
        val file: File? = FileChooser().apply {
            title = "New Money Database"
            initialDirectory = File(System.getProperty("user.home"))
            initialFileName = "Money$SUFFIX"
            extensionFilters.addAll(
                    FileChooser.ExtensionFilter("Money Database Files", "*$SUFFIX"),
                    FileChooser.ExtensionFilter("All Files", "*"))
        }.showSaveDialog(ownerWindow)

        file?.run {
            if (exists()) {
                if (!delete()) {

                    Alert(Alert.AlertType.ERROR).apply {
                        title = "Error"
                        contentText = "Could not delete existing file."
                    }.showAndWait()

                    return null
                }
            }
        }

        return file
    }

    override fun onUnsupportedVersion() {
        // this should not happen when creating a new file
        throw IllegalStateException("unsupported database version")
    }

    override fun onPendingUpgrades() = true

    override fun onConnectError(error: Throwable) {

        ErrorAlert.showAndWait(error)
    }
}
