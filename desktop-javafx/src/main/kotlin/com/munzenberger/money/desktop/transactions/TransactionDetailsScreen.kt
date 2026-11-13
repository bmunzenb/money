package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.desktop.accounts.AccountConverter
import com.munzenberger.money.desktop.components.FormGrid
import com.munzenberger.money.desktop.components.LoadStateContainer
import com.munzenberger.money.desktop.components.errorLabel
import com.munzenberger.money.desktop.components.setError
import com.munzenberger.money.desktop.components.setShown
import com.munzenberger.money.desktop.components.smallProgressIndicator
import com.munzenberger.money.desktop.components.withError
import com.munzenberger.money.desktop.components.withProgress
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.loadedOrEmpty
import com.munzenberger.money.presentation.transactions.TransactionDetailsUiState
import com.munzenberger.money.presentation.transactions.TransactionDetailsViewModel
import javafx.geometry.Insets
import javafx.scene.control.ComboBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.koin.core.parameter.parametersOf

/** Adds a new transaction at [Route.NewTransaction], or edits an existing one at [Route.TransactionDetails]. */
class TransactionDetailsScreen(scope: ScreenScope, route: Route.TransactionFormRoute) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: TransactionDetailsViewModel = scope.viewModel { parametersOf(route) }

    // Choosing the account the state already has, as render does, leaves the ViewModel's account unchanged.
    private val accountComboBox = ComboBox<Account>().apply {
        converter = AccountConverter
        setOnAction { value?.let(viewModel::onAccountChange) }
    }

    private val accountsProgress = smallProgressIndicator()

    private val accountsError = errorLabel(strings.getString("accounts_load_error_message"))

    private val fields = FormGrid().apply {
        addField(
            strings.getString("transaction_account_label"),
            accountComboBox,
            accountComboBox.withProgress(accountsProgress).withError(accountsError),
        )
    }

    private val form = VBox(Spacing.CONTROL_SPACING, fields)

    private val content = LoadStateContainer(form, strings.getString("transaction_load_error_message")).apply {
        setVgrow(this, Priority.ALWAYS)
    }

    init {
        prefWidth = PREF_WIDTH
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(content)
        scope.collect(viewModel.state, ::render)
    }

    /** Goes back as the ViewModel's back action does. */
    fun onCloseRequest() = viewModel.onBackClick()

    private fun render(state: TransactionDetailsUiState) {
        // A new transaction has nothing to load.
        content.setLoadState(state.existingTransaction ?: LoadState.Loaded(Unit))

        renderAccount(state)
    }

    private fun renderAccount(state: TransactionDetailsUiState) {
        val accounts = state.accounts.loadedOrEmpty
        if (accountComboBox.items != accounts) {
            accountComboBox.items.setAll(accounts)
        }
        accountComboBox.value = state.account
        accountComboBox.isDisable = state.accounts is LoadState.Loading
        accountComboBox.promptText = if (state.accounts is LoadState.Loading) {
            strings.getString("loading_prompt")
        } else {
            null
        }
        accountsProgress.setShown(state.accounts is LoadState.Loading)

        val isLoadError = state.accounts is LoadState.Error
        accountComboBox.setError(isLoadError)
        accountsError.setShown(isLoadError)
    }

    private companion object {
        const val PREF_WIDTH = 500.0
    }
}
