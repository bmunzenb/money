package com.munzenberger.money.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.register.GetAccountRegisterUseCase
import com.munzenberger.money.core.register.RegisterDateFilter
import com.munzenberger.money.core.register.RegisterStatusFilter
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransferEntryId
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.preferences.UserPreferences
import com.munzenberger.money.presentation.preferences.forAccount
import com.munzenberger.money.presentation.state.toLoadStates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class AccountRegisterViewModel(
    private val route: Route.AccountRegister,
    repositoryController: MoneyRepositoryController,
    getAccountRegister: GetAccountRegisterUseCase,
    private val navigator: Navigator,
    preferences: UserPreferences,
) : ViewModel() {

    // Each account keeps its own filters.
    private val accountPreferences = preferences.forAccount(route.accountId)

    private val dateFilter = MutableStateFlow(accountPreferences.registerFilters.dateFilter)

    private val statusFilter = MutableStateFlow(accountPreferences.registerFilters.statusFilter)

    // Starts with nothing loaded, so the selected filters show before the register arrives, and while no repository is
    // open, since combine waits for all of its flows to emit.
    private val register = repositoryController
        .flow { repository ->
            getAccountRegister(repository, route.accountId, dateFilter, statusFilter)
                .toLoadStates()
                .map { AccountRegisterUiState(register = it) }
        }
        .onStart { emit(AccountRegisterUiState()) }

    /**
     * Follows the route's account's register in the open repository, and stays loading while none is open. It starts
     * loading again when another repository opens. A missing account, such as one removed while its register is open,
     * fails like a failed read.
     *
     * The entries are those that match the selected date and status filters, which start at the filters last selected
     * on the account's register, or all dates and all transactions if none were.
     *
     * The balance is the last entry's balance before filtering, or the account's initial balance if it has no entries,
     * so the filters don't change it.
     *
     * The register's entries and ending balance keep the repository's signs for every account, and the state's
     * showsBalanceOwed and balance say to show a liability's balances as the amount owed.
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

    // The screen reports the filters it shows from the state, so a filter it already has is ignored rather than saved
    // again.
    fun onDateFilterChange(dateFilter: RegisterDateFilter) {
        if (dateFilter == this.dateFilter.value) return
        this.dateFilter.value = dateFilter
        saveFilters()
    }

    fun onStatusFilterChange(statusFilter: RegisterStatusFilter) {
        if (statusFilter == this.statusFilter.value) return
        this.statusFilter.value = statusFilter
        saveFilters()
    }

    private fun saveFilters() {
        accountPreferences.registerFilters = RegisterFilters(dateFilter.value, statusFilter.value)
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
