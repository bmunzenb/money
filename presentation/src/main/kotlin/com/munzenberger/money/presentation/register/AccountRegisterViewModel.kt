package com.munzenberger.money.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.account.GetAccountBalanceUseCase
import com.munzenberger.money.core.account.GetAccountUseCase
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.toLoadStates
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AccountRegisterViewModel(
    private val route: Route.AccountRegister,
    repositoryController: MoneyRepositoryController,
    getAccount: GetAccountUseCase,
    getAccountBalance: GetAccountBalanceUseCase,
    private val navigator: Navigator,
) : ViewModel() {

    /**
     * Follows the route's account and its balance in the open repository, and stays loading while none is open. Each
     * shows as soon as it arrives, and both start loading again when another repository opens. A missing account, such
     * as one removed while its register is open, fails like a failed read.
     */
    val state: StateFlow<AccountRegisterUiState> = repositoryController
        .flow { repository ->
            combine(
                getAccount(repository, route.accountId).toLoadStates(),
                getAccountBalance(repository, route.accountId).toLoadStates(),
                ::AccountRegisterUiState,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AccountRegisterUiState(),
        )

    fun onAccountSettingsClick() {
        navigator.navigate { add(Route.AccountSettings(route.accountId)) }
    }
}
