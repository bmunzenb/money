package com.munzenberger.money.desktop.welcome

import com.munzenberger.money.desktop.components.setShown
import com.munzenberger.money.desktop.database.DatabaseErrorAlert
import com.munzenberger.money.desktop.database.chooseDatabaseFile
import com.munzenberger.money.desktop.database.chooseNewDatabaseFile
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.welcome.WelcomeUiState
import com.munzenberger.money.presentation.welcome.WelcomeViewModel
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.VBox

class WelcomeScreen(scope: ScreenScope) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: WelcomeViewModel = scope.viewModel()

    private val openButton = Button(strings.getString("open_database_button_title")).apply {
        setOnAction { chooseDatabaseFile(scene.window)?.let(viewModel::openDatabase) }
    }

    private val createButton = Button(strings.getString("create_database_button_title")).apply {
        setOnAction { chooseNewDatabaseFile(scene.window)?.let(viewModel::createDatabase) }
    }

    private val demoButton = Button(strings.getString("open_demo_button_title")).apply {
        setOnAction { viewModel.openDemo() }
    }

    private val progressIndicator = ProgressIndicator()

    private val errorAlert = DatabaseErrorAlert(onDismiss = viewModel::dismissError)

    init {
        alignment = Pos.CENTER
        children.setAll(openButton, createButton, demoButton, progressIndicator)
        scope.collect(viewModel.state, ::render)
    }

    // While loading, shows only the progress indicator, so no button can start another operation.
    private fun render(state: WelcomeUiState) {
        openButton.setShown(!state.isLoading)
        createButton.setShown(!state.isLoading)
        demoButton.setShown(!state.isLoading)
        progressIndicator.setShown(state.isLoading)
        errorAlert.render(state.error, scene?.window)
    }
}
