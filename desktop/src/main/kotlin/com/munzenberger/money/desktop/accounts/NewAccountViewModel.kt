package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.data.api.account.AccountType
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
                val accountTypes = result.getOrDefault(emptyList()).sortedBy { it.value.ordinal }
                _state.update { it.copy(accountTypes = accountTypes) }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun onAccountTypeChange(accountType: AccountType) {
        _state.update { it.copy(accountType = accountType) }
    }

    fun onBackClick() {
        navigator.navigate { removeLast() }
    }
}
