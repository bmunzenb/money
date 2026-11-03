package com.munzenberger.money.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.core.account.GetAccountBalancesUseCase
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.preferences.UserPreferences
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.toLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class AccountListViewModel(
    repositoryController: MoneyRepositoryController,
    getAccountGroups: GetAccountGroupsUseCase,
    getAccountBalances: GetAccountBalancesUseCase,
    private val navigator: Navigator,
    private val preferences: UserPreferences,
) : ViewModel() {

    private val grouping = MutableStateFlow(preferences.accountGrouping)

    // The null result lets the selected grouping show before the first groups arrive, since combine
    // waits for all of its flows to emit.
    private val accountGroups = repositoryController
        .flow { getAccountGroups(it, grouping) }
        .onStart<Result<List<AccountGroup>>?> { emit(null) }

    // The null result lets the groups show before the balances arrive, with every balance loading.
    private val balanceResults = repositoryController
        .flow { getAccountBalances(it) }
        .onStart<Result<Map<AccountId, Money>>?> { emit(null) }

    val state: StateFlow<AccountListUiState> = combine(
        grouping,
        accountGroups,
        balanceResults,
    ) { selected, result, balances ->
        val groups = result?.toLoadState() ?: LoadState.Loading
        AccountListUiState(
            grouping = selected,
            groups = groups,
            balances = (groups as? LoadState.Loaded)?.let { accountBalances(it.value, balances) } ?: AccountBalances(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = AccountListUiState(grouping = grouping.value),
    )

    // The screen reports the grouping it shows from the state, so the grouping it already has is ignored rather than
    // saved again.
    fun onGroupingChange(grouping: AccountGrouping) {
        if (grouping == this.grouping.value) return
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
