package com.munzenberger.money.presentation.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.presentation.state.LoadState

/**
 * The state of an account's register. [entries] are the account's register entries, each with the account's balance
 * after it, and [balance] is the account's balance after the last of them. Their amounts keep the repository's signs.
 * [balance] stays loading until both [account] and [entries] load. [account] is an error if it can't be read, or if
 * there's no account with the route's id, such as after it's removed; [entries] is an error if they can't be read; and
 * [balance] is an error if either is.
 */
data class AccountRegisterUiState(
    val account: LoadState<Account> = LoadState.Loading,
    val balance: LoadState<Money> = LoadState.Loading,
    val entries: LoadState<List<RegisterEntry>> = LoadState.Loading,
) {
    /** Whether the account's settings can be opened: once the account is loaded, so there's an account to edit. */
    val canOpenAccountSettings: Boolean
        get() = account is LoadState.Loaded

    /** Whether a transaction can be added: once the account is loaded, so there's an account to add it to. */
    val canAddTransaction: Boolean
        get() = account is LoadState.Loaded
}
