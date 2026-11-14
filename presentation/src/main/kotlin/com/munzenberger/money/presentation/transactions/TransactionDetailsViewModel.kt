package com.munzenberger.money.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.transaction.GetTransactionUseCase
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.today
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.flow
import com.munzenberger.money.presentation.database.resultFlow
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.loadedOrEmpty
import com.munzenberger.money.presentation.state.loadedOrNull
import com.munzenberger.money.presentation.state.toLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * The transaction form, shown at [route]. For [Route.TransactionDetails], it edits that transaction, starting from its
 * saved values; for [Route.NewTransaction], it adds a new transaction.
 */
class TransactionDetailsViewModel(
    // This screen's route, so going back only pops this screen, even if a save finishes after the user has left it.
    private val route: Route.TransactionFormRoute,
    private val repositoryController: MoneyRepositoryController,
    private val navigator: Navigator,
    private val getTransaction: GetTransactionUseCase,
    // The date a new transaction starts on.
    today: () -> LocalDate = { LocalDate.today() },
) : ViewModel() {

    private val transactionId: TransactionId? = (route as? Route.TransactionDetails)?.transactionId

    // The account a new transaction starts in, if the route has one.
    private val newTransactionAccountId: AccountId? = (route as? Route.NewTransaction)?.accountId

    private val _state = MutableStateFlow(
        TransactionDetailsUiState(
            existingTransaction = transactionId?.let { LoadState.Loading },
            date = today(),
        )
    )
    val state: StateFlow<TransactionDetailsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repositoryController.resultFlow { it.accounts }.collect { result ->
                val accounts = result.map { accounts -> accounts.sortedBy { it.name.lowercase() } }.toLoadState()
                _state.update { it.copy(accounts = accounts).withInitialAccount() }
            }
        }
        if (transactionId != null) {
            viewModelScope.launch { loadExistingTransaction(transactionId) }
        }
    }

    /**
     * Fills in the form from the saved transaction. It's read only once, so a later change to the transaction
     * doesn't overwrite what the user has typed.
     */
    private suspend fun loadExistingTransaction(transactionId: TransactionId) {
        val transaction = repositoryController.flow { getTransaction(it, transactionId) }.first()
        _state.update { state ->
            val loaded = transaction.getOrNull()
            state.copy(
                existingTransaction = transaction.toLoadState(),
                date = loaded?.transaction?.date ?: state.date,
                type = loaded?.let { TransactionType.of(it.amount) } ?: state.type,
            ).withInitialAccount()
        }
    }

    /**
     * Selects the account the form starts in, once both it and the accounts are loaded: the existing transaction's
     * account, or the new transaction route's. Only while none is selected, so a reload keeps the user's choice.
     */
    private fun TransactionDetailsUiState.withInitialAccount(): TransactionDetailsUiState {
        if (account != null) return this
        val initialAccountId = existingTransaction?.loadedOrNull?.transaction?.accountId ?: newTransactionAccountId
        return copy(account = accounts.loadedOrEmpty.find { it.id == initialAccountId })
    }

    fun onAccountChange(account: Account) {
        _state.update { it.copy(account = account) }
    }

    fun onDateChange(date: LocalDate) {
        _state.update { it.copy(date = date) }
    }

    fun onTypeChange(type: TransactionType) {
        _state.update { it.copy(type = type) }
    }

    fun onBackClick() {
        navigator.back(from = route)
    }
}
