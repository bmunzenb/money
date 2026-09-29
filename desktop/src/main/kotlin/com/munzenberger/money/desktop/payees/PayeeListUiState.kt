package com.munzenberger.money.desktop.payees

import com.munzenberger.money.data.api.payee.Payee

sealed interface PayeeListUiState {
    data object Loading : PayeeListUiState
    data class Content(val payees: List<Payee> = emptyList()) : PayeeListUiState
}
