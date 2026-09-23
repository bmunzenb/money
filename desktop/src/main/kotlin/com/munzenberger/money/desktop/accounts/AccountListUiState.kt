package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.Account

data class AccountListUiState(
    val accounts: List<Account> = emptyList()
)
