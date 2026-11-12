package com.munzenberger.money.presentation.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClassConstant

/**
 * Whether the register shows this account's balances as the amount owed: a liability's balances are negated, so a debt
 * shows as a positive value.
 */
val Account.showsBalanceOwed: Boolean
    get() = accountType.accountClass.value == AccountClassConstant.Liability

/** This balance as the register shows it: negated if it's the amount [owed] (see [showsBalanceOwed]). */
fun Money.registerBalance(owed: Boolean): Money = if (owed) -this else this
