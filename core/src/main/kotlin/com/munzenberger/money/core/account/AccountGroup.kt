package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank

/** Accounts that share a value of an [AccountGrouping], sorted by name. */
sealed interface AccountGroup {
    val accounts: List<Account>

    data class All(override val accounts: List<Account>) : AccountGroup

    data class ByAccountType(val accountType: AccountType, override val accounts: List<Account>) : AccountGroup

    data class ByAccountClass(val accountClass: AccountClass, override val accounts: List<Account>) : AccountGroup

    /** The accounts at [bank], or those with no financial institution if it's null. */
    data class ByBank(val bank: Bank?, override val accounts: List<Account>) : AccountGroup
}
