package com.munzenberger.money.presentation.payees

import androidx.lifecycle.ViewModel
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.resultFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PayeeListViewModel(
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: Flow<PayeeListUiState> = repositoryController.resultFlow { it.payees }
        .map { result ->
            result.fold(
                onSuccess = { payees -> PayeeListUiState.Content(payees = payees) },
                onFailure = { PayeeListUiState.Error },
            )
        }

    fun onAddPayeeClick() {
        // Navigation to an add payee screen will go here once that route exists.
    }
}
