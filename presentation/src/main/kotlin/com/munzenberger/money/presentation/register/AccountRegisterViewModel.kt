package com.munzenberger.money.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.account.GetAccountBalanceUseCase
import com.munzenberger.money.core.account.GetAccountUseCase
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.toLoadState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class AccountRegisterViewModel(
    private val route: Route.AccountRegister,
    repositoryController: MoneyRepositoryController,
    getAccount: GetAccountUseCase,
    getAccountBalance: GetAccountBalanceUseCase,
    private val navigator: Navigator,
) : ViewModel() {

    // The null results let the account and its balance each show before the other arrives, since combine waits for
    // all of its flows to emit.
    private val accountResults = repositoryController
        .flow { getAccount(it, route.accountId) }
        .onStart<Result<Account>?> { emit(null) }

    private val balanceResults = repositoryController
        .flow { getAccountBalance(it, route.accountId) }
        .onStart<Result<Money>?> { emit(null) }

    /**
     * Follows the route's account and its balance in the open repository, and stays loading while none is open. A
     * missing account, such as one removed while its register is open, fails like a failed read.
     */
    val state: StateFlow<AccountRegisterUiState> = combine(accountResults, balanceResults) { account, balance ->
        AccountRegisterUiState(
            account = account?.toLoadState() ?: LoadState.Loading,
            balance = balance?.toLoadState() ?: LoadState.Loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = AccountRegisterUiState(),
    )

    fun onAccountSettingsClick() {
        navigator.navigate { add(Route.AccountSettings(route.accountId)) }
    }
}
