package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.Account

sealed interface AccountListUiState {
    data object Loading : AccountListUiState
    data class Content(val accounts: List<Account> = emptyList()) : AccountListUiState
}
