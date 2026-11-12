package com.munzenberger.money.presentation.register

import com.munzenberger.money.core.register.AccountRegister
import com.munzenberger.money.core.register.RegisterDateFilter
import com.munzenberger.money.core.register.RegisterStatusFilter
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.map

/**
 * The state of an account's register. [register] is the account, its register entries that match the selected
 * [dateFilter] and [statusFilter], and its ending balance (see [AccountRegister]), with the repository's signs. It's an
 * error if it can't be read, or if there's no account with the route's id, such as after it's removed.
 */
data class AccountRegisterUiState(
    val register: LoadState<AccountRegister> = LoadState.Loading,
    val dateFilter: RegisterDateFilter = RegisterDateFilter.AllDates,
    val statusFilter: RegisterStatusFilter = RegisterStatusFilter.AllTransactions,
) {
    /** Whether the account's settings can be opened: once the register is loaded, so there's an account to edit. */
    val canOpenAccountSettings: Boolean
        get() = register is LoadState.Loaded

    /** Whether a transaction can be added: once the register is loaded, so there's an account to add it to. */
    val canAddTransaction: Boolean
        get() = register is LoadState.Loaded

    /** The kind of account the amount columns are named for, once the register loads. */
    val amountColumns: LoadState<RegisterAmountColumns>
        get() = register.map { it.account.registerAmountColumns }
}
