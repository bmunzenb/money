package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.account.AccountInput
import com.munzenberger.money.core.account.AccountInputError
import com.munzenberger.money.core.account.CreateAccountUseCase
import com.munzenberger.money.core.account.SaveAccountResult
import com.munzenberger.money.core.account.parseInitialBalance
import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.desktop.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewAccountViewModel(
    repositoryController: MoneyRepositoryController,
    private val navigator: Navigator,
    private val createAccount: CreateAccountUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        NewAccountUiState(currencySymbol = Money.DEFAULT_CURRENCY.symbol)
    )
    val state: StateFlow<NewAccountUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repositoryController.resultFlow { it.accountTypes }.collect { result ->
                val accountTypes = result.map { types -> types.sortedBy { it.value.ordinal } }.toLoadState()
                _state.update { it.copy(accountTypes = accountTypes) }
            }
        }
        viewModelScope.launch {
            repositoryController.resultFlow { it.banks }.collect { result ->
                val banks = result.map { banks -> banks.sortedBy { it.name.lowercase() } }.toLoadState()
                _state.update { it.copy(banks = banks, bank = banks.loadedOrEmpty.matching(it.bankName)) }
            }
        }
    }

    fun onNameChange(name: String) {
        // Clear the error as soon as the name is filled in.
        _state.update { it.copy(name = name, isNameError = it.isNameError && name.isBlank()) }
    }

    fun onAccountTypeChange(accountType: AccountType) {
        _state.update { it.copy(accountType = accountType, isAccountTypeError = false) }
    }

    fun onBankNameChange(bankName: String) {
        _state.update { it.copy(bankName = bankName, bank = it.banks.loadedOrEmpty.matching(bankName)) }
    }

    fun onBankChange(bank: Bank) {
        _state.update { it.copy(bankName = bank.name, bank = bank) }
    }

    fun onNumberChange(number: String) {
        _state.update { it.copy(number = number) }
    }

    fun onInitialBalanceChange(initialBalance: String) {
        _state.update {
            // Clear the error as soon as the text becomes valid, but don't show one while still typing.
            it.copy(
                initialBalance = initialBalance,
                isInitialBalanceError = it.isInitialBalanceError && parseInitialBalance(initialBalance) == null,
            )
        }
    }

    fun onInitialBalanceFocusLost() {
        _state.update {
            val money = parseInitialBalance(it.initialBalance)
            when {
                it.initialBalance.isBlank() -> it.copy(initialBalance = "", isInitialBalanceError = false)
                money == null -> it.copy(isInitialBalanceError = true)
                else -> it.copy(initialBalance = money.toString(asCurrency = false), isInitialBalanceError = false)
            }
        }
    }

    fun onMemoChange(memo: String) {
        _state.update { it.copy(memo = memo) }
    }

    fun onSaveClick() {
        val current = _state.value
        if (current.saveState == SaveState.Saving) return

        _state.update { it.copy(saveState = SaveState.Saving) }

        viewModelScope.launch {
            when (val result = createAccount(current.toAccountInput())) {
                is SaveAccountResult.Success -> navigator.navigate { removeLast() }
                is SaveAccountResult.Invalid -> _state.update {
                    it.copy(
                        isNameError = AccountInputError.BlankName in result.errors,
                        isAccountTypeError = AccountInputError.MissingAccountType in result.errors,
                        isInitialBalanceError = AccountInputError.InvalidInitialBalance in result.errors,
                        saveState = SaveState.Idle,
                    )
                }
                is SaveAccountResult.Failure -> _state.update { it.copy(saveState = SaveState.Failed) }
            }
        }
    }

    fun onBackClick() {
        navigator.navigate { removeLast() }
    }
}

/**
 * Typing an existing bank's name selects that bank rather than starting a new one, so the user doesn't
 * end up with a duplicate just because they typed instead of picking from the list.
 */
private fun List<Bank>.matching(bankName: String): Bank? {
    val name = bankName.trim()
    return if (name.isEmpty()) null else firstOrNull { it.name.equals(name, ignoreCase = true) }
}

private fun NewAccountUiState.toAccountInput() = AccountInput(
    name = name,
    accountType = accountType,
    bankName = bankName,
    bank = bank,
    number = number,
    initialBalance = initialBalance,
    memo = memo,
)
