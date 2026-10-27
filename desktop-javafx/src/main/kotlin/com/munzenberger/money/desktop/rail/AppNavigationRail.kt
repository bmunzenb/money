package com.munzenberger.money.desktop.rail

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.rail.AppNavigationRailUiState
import com.munzenberger.money.presentation.rail.AppNavigationRailViewModel
import com.munzenberger.money.presentation.rail.TopLevelDestination
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignA
import org.kordamp.ikonli.materialdesign2.MaterialDesignS
import org.kordamp.ikonli.materialdesign2.MaterialDesignW

/** Lists the [TopLevelDestination]s, with the one [AppNavigationRailViewModel] selects shown as selected. */
class AppNavigationRail(scope: ScreenScope) : ListView<TopLevelDestination>() {
    private val viewModel: AppNavigationRailViewModel = scope.viewModel()

    // Set while render applies the state's selection, so it isn't taken for the user choosing a destination.
    private var isRendering = false

    init {
        items.setAll(TopLevelDestination.entries)
        prefWidth = PREF_WIDTH
        setCellFactory { DestinationCell() }

        // Covers choosing another destination with the mouse or the keyboard.
        selectionModel.selectedItemProperty().addListener { _, _, destination ->
            if (!isRendering && destination != null) viewModel.onDestinationClick(destination)
        }

        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AppNavigationRailUiState) {
        isRendering = true
        try {
            if (state.selected != null) selectionModel.select(state.selected) else selectionModel.clearSelection()
        } finally {
            isRendering = false
        }
    }

    // Clicking the selected destination doesn't change the selection, so the click itself reports it, e.g. to
    // go back to Accounts from a screen pushed on top of it.
    private inner class DestinationCell : ListCell<TopLevelDestination>() {
        // Follows the text fill, so the icon changes color with the text when the cell is selected.
        private val icon = FontIcon().apply { iconColorProperty().bind(textFillProperty()) }

        init {
            setOnMouseClicked {
                if (!isEmpty) viewModel.onDestinationClick(item)
            }
        }

        override fun updateItem(item: TopLevelDestination?, empty: Boolean) {
            super.updateItem(item, empty)
            text = if (empty || item == null) null else item.label
            graphic = if (empty || item == null) null else icon.apply { iconCode = item.icon }
        }
    }

    private val TopLevelDestination.label: String
        get() = strings.getString(
            when (this) {
                TopLevelDestination.Accounts -> "accounts_button_title"
                TopLevelDestination.Categories -> "categories_button_title"
                TopLevelDestination.Payees -> "payees_button_title"
            }
        )

    // Matches the icons desktop-material's TopLevelDestinationIcons uses.
    private val TopLevelDestination.icon: Ikon
        get() = when (this) {
            TopLevelDestination.Accounts -> MaterialDesignW.WALLET
            TopLevelDestination.Categories -> MaterialDesignS.SHAPE
            TopLevelDestination.Payees -> MaterialDesignA.ACCOUNT_GROUP
        }

    private companion object {
        const val PREF_WIDTH = 160.0
    }
}
