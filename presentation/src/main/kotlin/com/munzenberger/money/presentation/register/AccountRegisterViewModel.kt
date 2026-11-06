package com.munzenberger.money.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.account.GetAccountUseCase
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.toLoadState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AccountRegisterViewModel(
    route: Route.AccountRegister,
    repositoryController: MoneyRepositoryController,
    getAccount: GetAccountUseCase,
) : ViewModel() {

    /** Follows the route's account in the open repository, and stays loading while none is open. */
    val state: StateFlow<AccountRegisterUiState> = repositoryController
        .flow { getAccount(it, route.accountId) }
        // A missing account, such as one removed while its register is open, fails like a failed read.
        .map { result -> AccountRegisterUiState(account = result.toLoadState()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AccountRegisterUiState(),
        )
}
