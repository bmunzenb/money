package com.munzenberger.money.presentation.register

import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.presentation.state.LoadState

// An account's register shows its amounts in the account's sign: a liability's are negated, so what's owed on it shows
// as positive, and an asset's are as they are. The repository's amounts keep their own signs; this is only how they're
// shown.

/**
 * This state's value in [account]'s sign: negated by [negate] if the account is a liability, or as it is otherwise.
 * It's loading while the account loads, and an error if the account can't be loaded, since its sign isn't known.
 */
internal fun <T> LoadState<T>.inSignOf(account: LoadState<Account>, negate: (T) -> T): LoadState<T> = when (account) {
    LoadState.Loading -> LoadState.Loading
    LoadState.Error -> LoadState.Error
    is LoadState.Loaded -> when {
        this is LoadState.Loaded && account.value.isLiability -> LoadState.Loaded(negate(value))
        else -> this
    }
}

internal val Account.isLiability: Boolean
    get() = accountType.accountClass.value == AccountClassConstant.Liabilities

/** This entry with its amount, its balance, and its transaction's entries' amounts negated. */
internal fun RegisterEntry.negated(): RegisterEntry = when (this) {
    is RegisterEntry.Transaction -> copy(
        amount = -amount,
        balance = -balance,
        entries = entries.map { it.negated() },
    )
    is RegisterEntry.Transfer -> copy(amount = -amount, balance = -balance)
}

private fun RegisterEntry.Transaction.Entry.negated(): RegisterEntry.Transaction.Entry = when (this) {
    is RegisterEntry.Transaction.Entry.Category -> copy(amount = -amount)
    is RegisterEntry.Transaction.Entry.Transfer -> copy(amount = -amount)
}
