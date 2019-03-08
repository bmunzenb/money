package com.munzenberger.money.app.database

import com.munzenberger.money.core.MoneyDatabase
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

object OpenFileDatabaseConnector {

    private val logger = Logger.getLogger(OpenFileDatabaseConnector::class.java.simpleName)

    fun connect(ownerWindow: Window, success: (MoneyDatabase) -> Unit) {

        val file: File? = FileChooser().apply {
            title = "Open Money Database"
            initialDirectory = File(System.getProperty("user.home"))
            extensionFilters.addAll(
                    FileChooser.ExtensionFilter("Money Database Files", "*${FileDatabaseConnector.SUFFIX}"),
                    FileChooser.ExtensionFilter("All Files", "*"))
        }.showOpenDialog(ownerWindow)

        file?.run {
            FileDatabaseConnector.connect(this, object : DatabaseConnector.Callback {

                override fun onConnectSuccess(database: MoneyDatabase) {
                    success.invoke(database)
                }

                override fun onConnectPendingUpgrades(): Boolean {
                    val result = Alert(Alert.AlertType.CONFIRMATION).apply {
                        title = "Confirm Upgrade"
                        headerText = "Database upgrade required."
                        contentText = "The database file requires an upgrade to work with this version of Money. This operation cannot be undone. It is recommended you make a backup of your existing file before upgrading it. Would you like to proceed with the upgrade?"
                    }.showAndWait()

                    return result.isPresent && result.get() == ButtonType.OK
                }

                override fun onConnectUnsupportedVersion() {
                    Alert(Alert.AlertType.ERROR).apply {
                        title = "Error"
                        headerText = "Could not open database file."
                        contentText = "The database file is unsupported by this version of Money. This is likely due to having used the database file with a newer version of Money. Please update your version of Money and try again."
                    }.showAndWait()
                }

                override fun onConnectError(error: Throwable) {
                    logger.log(Level.WARNING, error) { "could not open database file" }

                    Alert(Alert.AlertType.ERROR).apply {
                        title = "Error"
                        headerText = "Could not open database file."
                        contentText = error.message
                    }.showAndWait()
                }
            })
        }
    }
}
