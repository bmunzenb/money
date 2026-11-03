package com.munzenberger.money.core.account

/** How [GetAccountGroupsUseCase] groups the accounts. */
enum class AccountGrouping {
    /** A single group with all the accounts. */
    None,
    AccountType,
    AccountClass,
    Bank,
}
