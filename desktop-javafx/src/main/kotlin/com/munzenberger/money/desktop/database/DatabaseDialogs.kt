package com.munzenberger.money.desktop.database

import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.database.DatabaseError
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

private const val MONEY_DATABASE_FILE_EXTENSION = "mdb"

/** Asks where to create a new database, returning the file chosen, or `null` if the user cancels. */
fun chooseNewDatabaseFile(owner: Window?): File? {
    val chooser = FileChooser().apply {
        title = strings.getString("create_database_dialog_title")
        initialFileName = "${strings.getString("default_file_name")}.$MONEY_DATABASE_FILE_EXTENSION"
        extensionFilters += FileChooser.ExtensionFilter(
            strings.getString("database_file_type"),
            "*.$MONEY_DATABASE_FILE_EXTENSION",
        )
    }
    return chooser.showSaveDialog(owner)
}

/** Asks for a database to open, returning the file chosen, or `null` if the user cancels. */
fun chooseDatabaseFile(owner: Window?): File? {
    val chooser = FileChooser().apply {
        title = strings.getString("open_database_dialog_title")
        extensionFilters += FileChooser.ExtensionFilter(
            strings.getString("database_file_type"),
            "*.$MONEY_DATABASE_FILE_EXTENSION",
        )
        extensionFilters += FileChooser.ExtensionFilter(
            strings.getString("all_file_type"),
            "*",
        )
    }
    return chooser.showOpenDialog(owner)
}

/**
 * Shows a UI state's [DatabaseError] in an alert, one at a time: [render] shows the error if no alert is already
 * showing, and [onDismiss] is called however the alert is closed, to clear the error in the ViewModel.
 */
class DatabaseErrorAlert(private val onDismiss: () -> Unit) {
    private var alert: Alert? = null

    fun render(error: DatabaseError?, owner: Window?) {
        if (error == null || alert != null) return
        alert = Alert(Alert.AlertType.ERROR, strings.getString(error.messageKey)).apply {
            headerText = null
            owner?.let(::initOwner)
            setOnHidden {
                alert = null
                onDismiss()
            }
            show()
        }
    }

    private val DatabaseError.messageKey: String
        get() = when (this) {
            DatabaseError.CreateDatabaseFailed -> "create_database_error_message"
            DatabaseError.OpenDatabaseFailed -> "open_database_error_message"
            DatabaseError.OpenDemoFailed -> "open_demo_error_message"
        }
}
