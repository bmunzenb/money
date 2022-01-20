package com.munzenberger.money.app.database

import com.munzenberger.money.app.ErrorAlert
import com.munzenberger.money.app.database.FileDatabaseConnector.Companion.SUFFIX
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

object NewFileDatabase : DatabaseConnectorCallbacks {

    fun openFile(ownerWindow: Window): File? {
        val file: File? = FileChooser().let {
            it.title = "New Money Database"
            it.initialDirectory = File(System.getProperty("user.home"))
            it.initialFileName = "Money$SUFFIX"
            it.extensionFilters.addAll(
                    FileChooser.ExtensionFilter("Money Database Files", "*$SUFFIX"),
                    FileChooser.ExtensionFilter("All Files", "*"))
            it.showSaveDialog(ownerWindow)
        }

        file?.run {
            if (exists()) {
                if (!delete()) {

                    Alert(Alert.AlertType.ERROR).apply {
                        title = "Error"
                        contentText = "Could not delete existing file."
                        showAndWait()
                    }

                    return null
                }
            }
        }

        return file
    }

    override fun onCanceled() {
        // Do nothing
    }

    override fun onConnected(database: ObservableMoneyDatabase, isFirstUse: Boolean) {
        // Do nothing
    }

    override fun onUnsupportedVersion() {
        // this should not happen when creating a new file
        ErrorAlert.showAndWait(IllegalStateException("Received unsupported database error while creating new database file."))
    }

    override fun onPendingUpgrades(): Boolean {
        // always apply updates
        return true
    }

    override fun onConnectError(error: Throwable) {
        ErrorAlert.showAndWait(error)
    }
}
