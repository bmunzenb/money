package com.munzenberger.money.desktop.payees

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PayeeListViewModel(
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: Flow<PayeeListUiState> = repositoryController.flow { it.payees }
        .map { payees -> PayeeListUiState.Content(payees = payees) }
}
