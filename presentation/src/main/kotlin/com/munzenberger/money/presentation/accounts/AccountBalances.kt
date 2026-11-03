package com.munzenberger.money.presentation.accounts

import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.toLoadState

/** Each account's balance, each group's subtotal, and the total of all of the groups' subtotals. */
data class AccountBalances(
    val accounts: Map<AccountId, LoadState<Money>> = emptyMap(),
    val subtotals: Map<AccountGroup, LoadState<Money>> = emptyMap(),
    val total: LoadState<Money> = LoadState.Loading,
) {
    fun balance(account: Account): LoadState<Money> = accounts[account.id] ?: LoadState.Loading

    fun subtotal(group: AccountGroup): LoadState<Money> = subtotals[group] ?: LoadState.Loading
}

/**
 * The balances of [groups]' accounts from [result], with each group's subtotal and the total of the subtotals (see
 * [sum]). Every balance is loading while [result] is null, and an error if it's a failure. An account missing from
 * the balances, such as one added since they were read, is still loading.
 */
internal fun accountBalances(
    groups: List<AccountGroup>,
    result: Result<Map<AccountId, Money>>?,
): AccountBalances {
    val accounts = groups.flatMap { it.accounts }.associate { account ->
        val balance = when {
            result == null -> LoadState.Loading
            result.isFailure -> LoadState.Error
            else -> result.getOrThrow()[account.id]?.let { LoadState.Loaded(it) } ?: LoadState.Loading
        }
        account.id to balance
    }
    val subtotals = groups.associateWith { group -> group.accounts.map { accounts.getValue(it.id) }.sum() }
    return AccountBalances(accounts, subtotals, total = subtotals.values.toList().sum())
}

/**
 * The sum of these amounts once they've all loaded, or zero if there are none. It's an error if any of them failed,
 * even while others are still loading, since the sum can't be calculated. A sum that overflows or mixes currencies is
 * also an error.
 */
private fun List<LoadState<Money>>.sum(): LoadState<Money> = when {
    any { it is LoadState.Error } -> LoadState.Error
    any { it is LoadState.Loading } -> LoadState.Loading
    else -> runCatching {
        map { (it as LoadState.Loaded).value }.reduceOrNull(Money::plus) ?: Money(0)
    }.toLoadState()
}
