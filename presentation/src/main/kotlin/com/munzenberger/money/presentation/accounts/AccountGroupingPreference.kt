package com.munzenberger.money.presentation.accounts

import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.presentation.preferences.PreferenceKey
import com.munzenberger.money.presentation.preferences.UserPreferences
import kotlinx.serialization.serializer

private val accountGroupingKey = PreferenceKey("account.list.grouping", AccountGrouping.None, serializer())

/** The grouping last selected on the account list. */
var UserPreferences.accountGrouping: AccountGrouping
    get() = get(accountGroupingKey)
    set(value) = set(accountGroupingKey, value)
