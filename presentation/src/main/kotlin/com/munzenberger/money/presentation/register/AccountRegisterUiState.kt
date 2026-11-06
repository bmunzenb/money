package com.munzenberger.money.presentation.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.presentation.state.LoadState

/**
 * The state of an account's register. [account] and [balance] are each an error if they can't be read, or if there's
 * no account with the route's id, such as after it's removed.
 */
data class AccountRegisterUiState(
    val account: LoadState<Account> = LoadState.Loading,
    val balance: LoadState<Money> = LoadState.Loading,
)
