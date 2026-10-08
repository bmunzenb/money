package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountListViewModel(
    getAccountGroups: GetAccountGroupsUseCase,
    private val navigator: Navigator,
) : ViewModel() {

    val state: Flow<AccountListUiState> = getAccountGroups(AccountGrouping.None)
        .map { result ->
            result.fold(
                onSuccess = { groups -> AccountListUiState.Content(groups = groups) },
                onFailure = { AccountListUiState.Error },
            )
        }

    fun onAddAccountClick() {
        navigator.navigate { add(Route.NewAccount) }
    }
}
