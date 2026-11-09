package com.munzenberger.money.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.account.GetAccountUseCase
import com.munzenberger.money.core.register.GetAccountRegisterUseCase
import com.munzenberger.money.core.register.RegisterDateFilter
import com.munzenberger.money.core.register.RegisterStatusFilter
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransferEntryId
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.toLoadStates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AccountRegisterViewModel(
    private val route: Route.AccountRegister,
    repositoryController: MoneyRepositoryController,
    getAccount: GetAccountUseCase,
    getAccountRegister: GetAccountRegisterUseCase,
    private val navigator: Navigator,
) : ViewModel() {

    private val dateFilter = MutableStateFlow(RegisterDateFilter.AllDates)

    private val statusFilter = MutableStateFlow(RegisterStatusFilter.AllTransactions)

    /**
     * Follows the route's account and its register entries in the open repository, and stays loading while none is
     * open. Both start loading again when another repository opens. A missing account, such as one removed while its
     * register is open, fails like a failed read.
     *
     * The balance is the last entry's balance, or the account's initial balance if it has no entries, rather than a
     * separate read, so it always agrees with the entries shown. Since it depends on the account, it stays loading
     * until the account loads, and fails if it does. The entries load independently of the account.
     *
     * Amounts keep the repository's signs for every account, including liabilities.
     */
    val state: StateFlow<AccountRegisterUiState> = repositoryController
        .flow { repository ->
            combine(
                getAccount(repository, route.accountId).toLoadStates(),
                getAccountRegister(repository, route.accountId, dateFilter, statusFilter).toLoadStates(),
            ) { account, entries ->
                AccountRegisterUiState(
                    account = account,
                    balance = entries.endingBalance(account),
                    entries = entries,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AccountRegisterUiState(),
        )

    fun onAccountSettingsClick() {
        if (!state.value.canOpenAccountSettings) return
        navigator.navigate { add(Route.AccountSettings(route.accountId)) }
    }

    fun onNewTransactionClick() {
        if (!state.value.canAddTransaction) return
        navigator.navigate { add(Route.NewTransaction(route.accountId)) }
    }

    fun onTransactionClick(transactionId: TransactionId) {
        navigator.navigate { add(Route.TransactionDetails(transactionId)) }
    }

    fun onTransferClick(transferEntryId: TransferEntryId) {
        navigator.navigate { add(Route.TransferDetails(transferEntryId)) }
    }
}

/**
 * The account's balance after these entries: the last one's balance, or [account]'s initial balance if there are none.
 * It's loading or an error while the entries or the account are.
 */
private fun LoadState<List<RegisterEntry>>.endingBalance(account: LoadState<Account>): LoadState<Money> = when {
    this is LoadState.Error || account is LoadState.Error -> LoadState.Error
    this is LoadState.Loaded && account is LoadState.Loaded ->
        LoadState.Loaded(value.lastOrNull()?.balance ?: account.value.initialBalance)
    else -> LoadState.Loading
}
