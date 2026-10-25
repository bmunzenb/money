package com.munzenberger.money.desktop.welcome

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.welcome.WelcomeError
import com.munzenberger.money.presentation.welcome.WelcomeUiState
import com.munzenberger.money.presentation.welcome.WelcomeViewModel
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.VBox
import javafx.stage.FileChooser

class WelcomeScreen(scope: ScreenScope) : VBox(SPACING) {
    private val viewModel: WelcomeViewModel = scope.viewModel()

    private val openButton = Button(strings.getString("open_database_button_title")).apply {
        setOnAction { openDatabase() }
    }

    private val createButton = Button(strings.getString("create_database_button_title")).apply {
        setOnAction { createDatabase() }
    }

    private val progressIndicator = ProgressIndicator()

    private var errorAlert: Alert? = null

    init {
        alignment = Pos.CENTER
        children.setAll(openButton, createButton, progressIndicator)
        scope.collect(viewModel.state, ::render)
    }

    // While loading, shows only the progress indicator, so neither button can start another operation.
    private fun render(state: WelcomeUiState) {
        openButton.setShown(!state.isLoading)
        createButton.setShown(!state.isLoading)
        progressIndicator.setShown(state.isLoading)

        val error = state.error
        if (error != null && errorAlert == null) {
            showError(error)
        }
    }

    // Dismisses the error in the ViewModel however the dialog is closed.
    private fun showError(error: WelcomeError) {
        val owner = scene?.window
        errorAlert = Alert(Alert.AlertType.ERROR, strings.getString(error.messageKey)).apply {
            headerText = null
            owner?.let(::initOwner)
            setOnHidden {
                errorAlert = null
                viewModel.dismissError()
            }
            show()
        }
    }

    private fun openDatabase() {
        val chooser = FileChooser().apply {
            title = strings.getString("open_database_dialog_title")
        }
        chooser.showOpenDialog(scene.window)?.let(viewModel::openDatabase)
    }

    private fun createDatabase() {
        val chooser = FileChooser().apply {
            title = strings.getString("create_database_dialog_title")
            initialFileName = "${strings.getString("default_file_name")}.$MONEY_DATABASE_FILE_EXTENSION"
            extensionFilters += FileChooser.ExtensionFilter(
                strings.getString("database_file_type"),
                "*.$MONEY_DATABASE_FILE_EXTENSION",
            )
        }
        chooser.showSaveDialog(scene.window)?.let(viewModel::createDatabase)
    }

    // Hidden nodes also give up their space in the layout.
    private fun Node.setShown(shown: Boolean) {
        isVisible = shown
        isManaged = shown
    }

    private val WelcomeError.messageKey: String
        get() = when (this) {
            WelcomeError.CreateDatabaseFailed -> "create_database_error_message"
            WelcomeError.OpenDatabaseFailed -> "open_database_error_message"
        }

    private companion object {
        const val SPACING = 8.0
        const val MONEY_DATABASE_FILE_EXTENSION = "mdb"
    }
}
