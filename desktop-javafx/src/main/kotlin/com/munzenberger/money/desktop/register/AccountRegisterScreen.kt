package com.munzenberger.money.desktop.register

import com.munzenberger.money.desktop.components.ScreenHeading
import com.munzenberger.money.desktop.components.errorLabel
import com.munzenberger.money.desktop.components.smallProgressIndicator
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.register.AccountRegisterUiState
import com.munzenberger.money.presentation.register.AccountRegisterViewModel
import com.munzenberger.money.presentation.state.LoadState
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.koin.core.parameter.parametersOf

/** An account's register, headed by the account's name. */
class AccountRegisterScreen(scope: ScreenScope, route: Route.AccountRegister) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: AccountRegisterViewModel = scope.viewModel { parametersOf(route) }

    private val headingProgress = smallProgressIndicator()

    private val heading = ScreenHeading("")

    private val headingError = errorLabel(strings.getString("account_load_error_message"))

    // Holds the progress indicator while the account loads, its name once it has, or the error message if it can't be
    // loaded or no longer exists.
    private val header = HBox().apply {
        alignment = Pos.CENTER_LEFT
    }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(header)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AccountRegisterUiState) {
        val headerContent = when (val account = state.account) {
            LoadState.Loading -> headingProgress
            LoadState.Error -> headingError
            is LoadState.Loaded -> heading.apply { text = account.value.name }
        }
        header.children.setAll(headerContent)
    }
}
