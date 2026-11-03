package com.munzenberger.money.presentation.payees

import androidx.lifecycle.ViewModel
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.resultFlow
import com.munzenberger.money.presentation.state.toLoadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PayeeListViewModel(
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: Flow<PayeeListUiState> = repositoryController.resultFlow { it.payees }
        .map { PayeeListUiState(payees = it.toLoadState()) }

    fun onAddPayeeClick() {
        // Navigation to an add payee screen will go here once that route exists.
    }
}
