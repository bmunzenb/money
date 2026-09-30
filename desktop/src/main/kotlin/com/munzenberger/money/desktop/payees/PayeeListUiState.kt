package com.munzenberger.money.desktop.payees

import com.munzenberger.money.data.api.payee.Payee

sealed interface PayeeListUiState {
    data object Loading : PayeeListUiState
    data object Error : PayeeListUiState
    data class Content(val payees: List<Payee> = emptyList()) : PayeeListUiState
}
