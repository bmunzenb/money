package com.munzenberger.money.desktop.welcome

import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.welcome.WelcomeViewModel
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.FileChooser

class WelcomeScreen(
    private val viewModel: WelcomeViewModel,
) : VBox(SPACING) {
    init {
        alignment = Pos.CENTER
        children.setAll(
            Button(strings.getString("open_database_button_title")).apply {
                setOnAction { openDatabase() }
            },
            Button(strings.getString("create_database_button_title")).apply {
                setOnAction { createDatabase() }
            },
        )
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

    private companion object {
        const val SPACING = 8.0
        const val MONEY_DATABASE_FILE_EXTENSION = "mdb"
    }
}
