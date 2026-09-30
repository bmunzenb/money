package com.munzenberger.money.desktop.payees

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.resultFlow
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
}
