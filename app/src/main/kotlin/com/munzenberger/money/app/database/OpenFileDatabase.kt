package com.munzenberger.money.app.database

import com.munzenberger.money.app.ErrorAlert
import com.munzenberger.money.app.database.FileDatabaseConnector.Companion.SUFFIX
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

object OpenFileDatabase : DatabaseConnectorCallbacks {

    fun openFile(ownerWindow: Window): File? = FileChooser().apply {
        title = "Open Money Database"
        initialDirectory = File(System.getProperty("user.home"))
        extensionFilters.addAll(
                FileChooser.ExtensionFilter("Money Database Files", "*$SUFFIX"),
                FileChooser.ExtensionFilter("All Files", "*"))
    }.showOpenDialog(ownerWindow)

    override fun onCanceled() {
        // Do nothing
    }

    override fun onConnected(database: ObservableMoneyDatabase, isFirstUse: Boolean) {
        // Do nothing
    }

    override fun onUnsupportedVersion() {

        Alert(Alert.AlertType.ERROR).apply {
            title = "Error"
            headerText = "Could not open database file."
            contentText = "The database file is unsupported by this version of Money. This is likely due to having used the database file with a newer version of Money. Please update your version of Money and try again."
        }.showAndWait()
    }

    override fun onPendingUpgrades(): Boolean {

        val result = Alert(Alert.AlertType.CONFIRMATION).let {
            it.title = "Confirm Upgrade"
            it.headerText = "Database upgrade required."
            it.contentText = "The database file requires an upgrade to work with this version of Money. This operation cannot be undone. It is recommended you make a backup of your existing file before upgrading it. Would you like to proceed with the upgrade?"
            it.showAndWait()
        }

        return result.isPresent && result.get() == ButtonType.OK
    }

    override fun onConnectError(error: Throwable) {
        ErrorAlert(error).showAndWait()
    }
}
