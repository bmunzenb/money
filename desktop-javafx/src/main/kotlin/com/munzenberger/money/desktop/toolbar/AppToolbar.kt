package com.munzenberger.money.desktop.toolbar

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.toolbar.AppToolbarUiState
import com.munzenberger.money.presentation.toolbar.AppToolbarViewModel
import javafx.scene.control.Button
import javafx.scene.control.ToolBar
import org.kordamp.ikonli.coreui.CoreUiFree
import org.kordamp.ikonli.javafx.FontIcon

/** The app's toolbar, with a back button that [AppToolbarViewModel] enables while there's a route to go back to. */
class AppToolbar(scope: ScreenScope) : ToolBar() {
    private val viewModel: AppToolbarViewModel = scope.viewModel()

    private val backButton = Button(null, FontIcon(CoreUiFree.ARROW_LEFT)).apply {
        accessibleText = strings.getString("back_button_accessible_text")
        setOnAction { viewModel.onBackClick() }
    }

    init {
        items.setAll(backButton)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AppToolbarUiState) {
        backButton.isDisable = !state.canNavigateBack
    }
}
