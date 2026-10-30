package com.munzenberger.money.desktop.rail

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.rail.AppNavigationRailUiState
import com.munzenberger.money.presentation.rail.AppNavigationRailViewModel
import com.munzenberger.money.presentation.rail.TopLevelDestination
import javafx.scene.control.ListCell
import javafx.scene.control.ListView

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
        init {
            setOnMouseClicked {
                if (!isEmpty) viewModel.onDestinationClick(item)
            }
        }

        override fun updateItem(item: TopLevelDestination?, empty: Boolean) {
            super.updateItem(item, empty)
            text = if (empty || item == null) null else item.label
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

    private companion object {
        const val PREF_WIDTH = 160.0
    }
}
