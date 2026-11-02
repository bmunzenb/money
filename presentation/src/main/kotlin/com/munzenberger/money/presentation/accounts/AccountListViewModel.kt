package com.munzenberger.money.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class AccountListViewModel(
    repositoryController: MoneyRepositoryController,
    getAccountGroups: GetAccountGroupsUseCase,
    private val navigator: Navigator,
    private val preferences: UserPreferences,
) : ViewModel() {

    private val grouping = MutableStateFlow(preferences.accountGrouping)

    // The null result lets the selected grouping show before the first groups arrive, since combine
    // waits for both flows to emit.
    private val accountGroups = repositoryController
        .flow { getAccountGroups(it, grouping) }
        .onStart<Result<List<AccountGroup>>?> { emit(null) }

    val state: StateFlow<AccountListUiState> = combine(grouping, accountGroups) { selected, result ->
        AccountListUiState(grouping = selected, groups = result?.toLoadState() ?: LoadState.Loading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = AccountListUiState(grouping = grouping.value),
    )

    fun onGroupingChange(grouping: AccountGrouping) {
        this.grouping.value = grouping
        preferences.accountGrouping = grouping
    }

    fun onNewAccountClick() {
        navigator.navigate { add(Route.NewAccount) }
    }

    fun onAccountClick(account: Account) {
        navigator.navigate { add(Route.TransactionList(account.id)) }
    }

    fun onAccountSettingsClick(account: Account) {
        navigator.navigate { add(Route.AccountSettings(account.id)) }
    }
}
