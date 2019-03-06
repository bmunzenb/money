package com.munzenberger.money.app.database

import com.munzenberger.money.core.MoneyDatabase
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

class OpenFileDatabaseConnector(private val ownerWindow: Window) {

    fun connect(completionHandler: (MoneyDatabase) -> Unit) {

        val file: File? = FileChooser().apply {
            title = "Open Money Database"
            initialDirectory = File(System.getProperty("user.home"))
            extensionFilters.add(FileChooser.ExtensionFilter("Money Database Files", "*${FileDatabaseConnector.SUFFIX}"))
        }.showOpenDialog(ownerWindow)

        file?.run {
            FileDatabaseConnector().connect(this, object : DatabaseConnector.Callback {

                override fun onConnectPendingUpgrades(): Boolean {
                    val result = Alert(Alert.AlertType.CONFIRMATION).apply {
                        title = "Confirm Upgrade"
                        headerText = "Database version upgrade required."
                        contentText = "The database file needs to be upgraded to work with this version of Money. This operation cannot be undone. It is recommended you make a backup of your existing file before upgrading it. Would you like to proceed with the upgrade?"
                    }.showAndWait()

                    return result.isPresent && result.get() == ButtonType.OK
                }

                override fun onConnectComplete(database: MoneyDatabase) {
                    completionHandler.invoke(database)
                }

                override fun onConnectUnsupportedVersion() {
                    Alert(Alert.AlertType.ERROR).apply {
                        title = "Error"
                        headerText = "Could not open database file."
                        contentText = "The database file is unsupported by this version of Money. This is likely due to having used the database file with a newer version of Money. Please update your version of Money and try again."
                    }.showAndWait()
                }

                override fun onConnectError(error: Throwable) {
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
