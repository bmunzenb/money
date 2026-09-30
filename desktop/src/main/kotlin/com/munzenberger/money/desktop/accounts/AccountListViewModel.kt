package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.resultFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountListViewModel(
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: Flow<AccountListUiState> = repositoryController.resultFlow { it.accounts }
        .map { result ->
            result.fold(
                onSuccess = { accounts -> AccountListUiState.Content(accounts = accounts) },
                onFailure = { AccountListUiState.Error },
            )
        }

    fun onAddAccountClick() {
        // Navigation to an add account screen will go here once that route exists.
    }
}
