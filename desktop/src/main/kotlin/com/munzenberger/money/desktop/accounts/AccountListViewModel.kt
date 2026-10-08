package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class AccountListViewModel(
    getAccountGroups: GetAccountGroupsUseCase,
    private val navigator: Navigator,
) : ViewModel() {

    private val grouping = MutableStateFlow(AccountGrouping.None)

    // Changing the grouping starts loading its groups, while the dropdown shows the new grouping.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<AccountListUiState> = grouping.flatMapLatest { grouping ->
        getAccountGroups(grouping)
            .map { result -> AccountListUiState(grouping = grouping, groups = result.toLoadState()) }
            .onStart { emit(AccountListUiState(grouping = grouping)) }
    }

    fun onGroupingChange(grouping: AccountGrouping) {
        this.grouping.value = grouping
    }

    fun onAddAccountClick() {
        navigator.navigate { add(Route.NewAccount) }
    }
}
