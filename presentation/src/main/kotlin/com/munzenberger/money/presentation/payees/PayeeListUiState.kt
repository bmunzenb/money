package com.munzenberger.money.presentation.payees

import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.presentation.state.LoadState

data class PayeeListUiState(
    val payees: LoadState<List<Payee>> = LoadState.Loading,
)
