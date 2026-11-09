package com.munzenberger.money.presentation.preferences

import com.munzenberger.money.data.api.account.AccountId

/** The preferences for the account with [accountId], in a child node named by its id. */
fun UserPreferences.forAccount(accountId: AccountId): UserPreferences = node(accountId.value.toString())
