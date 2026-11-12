package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank

/** The values entered for an account, as typed into the form. */
data class AccountInput(
    val name: String,
    val accountType: AccountType?,
    /** The financial institution's name: an existing bank's name, a new one, or blank for none. */
    val bankName: String,
    /** The existing bank to use, or null to add a bank named [bankName] (if it isn't blank). */
    val bank: Bank?,
    val number: String,
    /** The opening balance, as the amount owed for a liability; blank means zero. */
    val initialBalance: String,
    val memo: String,
)
