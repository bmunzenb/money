package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.transactions.TransactionDetailsViewModel
import javafx.geometry.Insets
import javafx.scene.layout.VBox
import org.koin.core.parameter.parametersOf

/** Adds a new transaction at [Route.NewTransaction], or edits an existing one at [Route.TransactionDetails]. */
class TransactionDetailsScreen(scope: ScreenScope, route: Route.TransactionFormRoute) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: TransactionDetailsViewModel = scope.viewModel { parametersOf(route) }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
    }

    /** Goes back as the ViewModel's back action does. */
    fun onCloseRequest() = viewModel.onBackClick()
}
