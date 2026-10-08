package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping

data class AccountListUiState(
    val grouping: AccountGrouping = AccountGrouping.None,
    val groups: LoadState<List<AccountGroup>> = LoadState.Loading,
)
