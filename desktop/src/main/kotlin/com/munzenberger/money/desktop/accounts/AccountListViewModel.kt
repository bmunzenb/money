package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountListViewModel(
    repositoryController: MoneyRepositoryController,
    private val navigator: Navigator,
) : ViewModel() {

    val state: Flow<AccountListUiState> = repositoryController.resultFlow { it.accounts }
        .map { result ->
            result.fold(
                onSuccess = { accounts -> AccountListUiState.Content(accounts = accounts) },
                onFailure = { AccountListUiState.Error },
            )
        }

    fun onAddAccountClick() {
        navigator.navigate { add(Route.NewAccount) }
    }
}
