package com.munzenberger.money.desktop.register

import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.desktop.components.AccountBalanceView
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
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.koin.core.parameter.parametersOf

/**
 * An account's register, headed by the account's name, with a row of controls to see the account's settings, a
 * table of its entries, and the account's balance below them.
 */
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

    private val accountSettingsButton = Button(strings.getString("account_settings_button_title")).apply {
        isDisable = true
        setOnAction { viewModel.onAccountSettingsClick() }
    }

    // Pushes the account settings button to the end of the row.
    private val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }

    private val controls = HBox(Spacing.CONTROL_SPACING).apply {
        alignment = Pos.CENTER_LEFT
        children.setAll(spacer, accountSettingsButton)
    }

    // Takes the rest of the screen's height.
    private val registerTable = TableView<RegisterEntry>().apply {
        setVgrow(this, Priority.ALWAYS)
    }

    // Stays at the bottom of the screen, since the table above it takes the rest of the height. Its labels inherit the
    // bold font.
    private val accountBalance = AccountBalanceView(strings.getString("account_balance_label")).apply {
        styleClass += "total-balance"
    }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(header, controls, registerTable, accountBalance)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AccountRegisterUiState) {
        val headerContent = when (val account = state.account) {
            LoadState.Loading -> headingProgress
            LoadState.Error -> headingError
            is LoadState.Loaded -> heading.apply { text = account.value.name }
        }
        header.children.setAll(headerContent)

        accountSettingsButton.isDisable = !state.canOpenAccountSettings

        accountBalance.render(state.balance)
    }
}
