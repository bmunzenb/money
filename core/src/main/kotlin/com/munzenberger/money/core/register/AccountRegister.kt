package com.munzenberger.money.core.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.register.RegisterEntry

/**
 * An account's register, as [GetAccountRegisterUseCase] returns it. [account] is the account, [entries] are the
 * register entries that match the filters, and [endingBalance] is the account's balance after all of its entries,
 * whether or not they match, or its initial balance if it has none.
 */
data class AccountRegister(
    val account: Account,
    val entries: List<RegisterEntry>,
    val endingBalance: Money,
)
