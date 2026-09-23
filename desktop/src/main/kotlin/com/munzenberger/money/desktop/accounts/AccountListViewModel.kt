package com.munzenberger.money.desktop.accounts

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountListViewModel(
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: Flow<AccountListUiState> = repositoryController.flow { it.accounts }
        .map { accounts -> AccountListUiState(accounts = accounts) }
}
