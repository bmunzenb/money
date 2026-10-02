package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.AccountType

data class NewAccountUiState(
    val name: String = "",
    val accountTypes: List<AccountType> = emptyList(),
    val accountType: AccountType? = null,
)
