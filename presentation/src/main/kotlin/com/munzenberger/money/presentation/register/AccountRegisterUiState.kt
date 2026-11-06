package com.munzenberger.money.presentation.register

import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.presentation.state.LoadState

/**
 * The state of an account's register. [account] is an error if it can't be read, or if there's no account with the
 * route's id, such as after it's removed.
 */
data class AccountRegisterUiState(
    val account: LoadState<Account> = LoadState.Loading,
)
