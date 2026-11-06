package com.munzenberger.money.presentation.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.presentation.state.LoadState

/**
 * The state of an account's register. [entries] are the account's register entries, each with the account's balance
 * after it. [balance] and [entries] are in the account's sign, so a liability's are negated to show what's owed on it
 * as positive, and they stay loading until [account] loads. Each is an error if it can't be read, or if there's no
 * account with the route's id, such as after it's removed.
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
