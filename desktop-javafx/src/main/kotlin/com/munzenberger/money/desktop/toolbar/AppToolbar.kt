package com.munzenberger.money.desktop.toolbar

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.toolbar.AppToolbarUiState
import com.munzenberger.money.presentation.toolbar.AppToolbarViewModel
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.Separator
import javafx.scene.control.ToolBar
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignA
import org.kordamp.ikonli.materialdesign2.MaterialDesignP

/**
 * The app's toolbar, with a back button that [AppToolbarViewModel] enables while there's a route to go back to, and a
 * button to add a transaction that it disables while the new transaction form is showing.
 */
class AppToolbar(scope: ScreenScope) : ToolBar() {
    private val viewModel: AppToolbarViewModel = scope.viewModel()

    private val backButton = Button(
        strings.getString("back_button_title"),
        FontIcon(MaterialDesignA.ARROW_LEFT),
    ).apply {
        setOnAction { viewModel.onBackClick() }
    }

    private val newTransactionButton = Button(
        strings.getString("new_transaction_button_title"),
        FontIcon(MaterialDesignP.PLUS),
    ).apply {
        setOnAction { viewModel.onNewTransactionClick() }
    }

    init {
        items.setAll(backButton, Separator(Orientation.VERTICAL), newTransactionButton)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AppToolbarUiState) {
        backButton.isDisable = !state.canNavigateBack
        newTransactionButton.isDisable = !state.canAddTransaction
    }
}
