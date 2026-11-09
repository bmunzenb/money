package com.munzenberger.money.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.account.GetAccountUseCase
import com.munzenberger.money.core.register.AccountRegister
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
import kotlinx.coroutines.flow.onStart
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

    // Starts with nothing loaded, so the selected filters show before the account and entries arrive, and while no
    // repository is open, since combine waits for all of its flows to emit.
    private val register = repositoryController
        .flow { repository ->
            combine(
                getAccount(repository, route.accountId).toLoadStates(),
                getAccountRegister(repository, route.accountId, dateFilter, statusFilter).toLoadStates(),
            ) { account, register ->
                AccountRegisterUiState(
                    account = account,
                    balance = register.endingBalance(account),
                    entries = register.entries(),
                )
            }
        }
        .onStart { emit(AccountRegisterUiState()) }

    /**
     * Follows the route's account and its register entries in the open repository, and stays loading while none is
     * open. Both start loading again when another repository opens. A missing account, such as one removed while its
     * register is open, fails like a failed read.
     *
     * The entries are those that match the selected date and status filters, which start at all dates and all
     * transactions.
     *
     * The balance is the last entry's balance before filtering, or the account's initial balance if it has no entries,
     * so the filters don't change it. It comes from the same read as the entries rather than a separate one, so it
     * always agrees with them. Since it depends on the account, it stays loading until the account loads, and fails if
     * it does. The entries load independently of the account.
     *
     * Amounts keep the repository's signs for every account, including liabilities.
     */
    val state: StateFlow<AccountRegisterUiState> = combine(
        dateFilter,
        statusFilter,
        register,
    ) { dateFilter, statusFilter, register ->
        register.copy(dateFilter = dateFilter, statusFilter = statusFilter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = AccountRegisterUiState(dateFilter = dateFilter.value, statusFilter = statusFilter.value),
    )

    fun onDateFilterChange(dateFilter: RegisterDateFilter) {
        this.dateFilter.value = dateFilter
    }

    fun onStatusFilterChange(statusFilter: RegisterStatusFilter) {
        this.statusFilter.value = statusFilter
    }

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

/** The register's entries that match the filters, loading or an error while the register is. */
private fun LoadState<AccountRegister>.entries(): LoadState<List<RegisterEntry>> = when (this) {
    LoadState.Loading -> LoadState.Loading
    LoadState.Error -> LoadState.Error
    is LoadState.Loaded -> LoadState.Loaded(value.entries)
}

/**
 * The account's balance after all of the register's entries, whether or not they match the filters, or [account]'s
 * initial balance if there are none. It's loading or an error while the register or the account are.
 */
private fun LoadState<AccountRegister>.endingBalance(account: LoadState<Account>): LoadState<Money> = when {
    this is LoadState.Error || account is LoadState.Error -> LoadState.Error
    this is LoadState.Loaded && account is LoadState.Loaded ->
        LoadState.Loaded(value.endingBalance ?: account.value.initialBalance)
    else -> LoadState.Loading
}
