package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGroup

sealed interface AccountListUiState {
    data object Loading : AccountListUiState
    data object Error : AccountListUiState
    data class Content(val groups: List<AccountGroup> = emptyList()) : AccountListUiState
}
