package com.munzenberger.money.desktop.payees

import com.munzenberger.money.data.api.payee.Payee

data class PayeeListUiState(
    val payees: List<Payee> = emptyList()
)
