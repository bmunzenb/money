package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.resultFlow
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
) : ViewModel() {

    private val _state = MutableStateFlow(NewAccountUiState())
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
        _state.update { it.copy(name = name) }
    }

    fun onAccountTypeChange(accountType: AccountType) {
        _state.update { it.copy(accountType = accountType) }
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
