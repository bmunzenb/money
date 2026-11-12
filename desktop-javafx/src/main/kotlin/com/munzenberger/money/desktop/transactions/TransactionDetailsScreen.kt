package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.LoadStateContainer
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.transactions.TransactionDetailsUiState
import com.munzenberger.money.presentation.transactions.TransactionDetailsViewModel
import javafx.geometry.Insets
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.koin.core.parameter.parametersOf

/** Adds a new transaction at [Route.NewTransaction], or edits an existing one at [Route.TransactionDetails]. */
class TransactionDetailsScreen(scope: ScreenScope, route: Route.TransactionFormRoute) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: TransactionDetailsViewModel = scope.viewModel { parametersOf(route) }

    private val form = VBox(Spacing.CONTROL_SPACING)

    private val content = LoadStateContainer(form, strings.getString("transaction_load_error_message")).apply {
        setVgrow(this, Priority.ALWAYS)
    }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(content)
        scope.collect(viewModel.state, ::render)
    }

    /** Goes back as the ViewModel's back action does. */
    fun onCloseRequest() = viewModel.onBackClick()

    private fun render(state: TransactionDetailsUiState) {
        // A new transaction has nothing to load.
        content.setLoadState(state.existingTransaction ?: LoadState.Loaded(Unit))
    }
}
