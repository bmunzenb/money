package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.presentation.accounts.AccountListUiState
import com.munzenberger.money.presentation.accounts.AccountListViewModel
import javafx.scene.layout.StackPane

/** Lists the accounts. Shows nothing yet. */
class AccountListScreen(scope: ScreenScope) : StackPane() {
    private val viewModel: AccountListViewModel = scope.viewModel()

    init {
        scope.collect(viewModel.state, ::render)
    }

    @Suppress("UnusedParameter")
    private fun render(state: AccountListUiState) {
        // Doesn't show the state yet.
    }
}
