package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

class AccountListViewModel(
    getAccountGroups: GetAccountGroupsUseCase,
    private val navigator: Navigator,
) : ViewModel() {

    private val grouping = MutableStateFlow(AccountGrouping.None)

    // The null result lets the selected grouping show before the first groups arrive; combine can
    // skip it when they arrive at once, so the state also starts with loading.
    val state: Flow<AccountListUiState> = combine(
        grouping,
        getAccountGroups(grouping).onStart<Result<List<AccountGroup>>?> { emit(null) },
    ) { selected, result ->
        AccountListUiState(grouping = selected, groups = result?.toLoadState() ?: LoadState.Loading)
    }
        .onStart { emit(AccountListUiState(grouping = grouping.value)) }
        .distinctUntilChanged()

    fun onGroupingChange(grouping: AccountGrouping) {
        this.grouping.value = grouping
    }

    fun onAddAccountClick() {
        navigator.navigate { add(Route.NewAccount) }
    }
}
