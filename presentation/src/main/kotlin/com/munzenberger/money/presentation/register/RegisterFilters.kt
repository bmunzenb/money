package com.munzenberger.money.presentation.register

import com.munzenberger.money.core.register.RegisterDateFilter
import com.munzenberger.money.core.register.RegisterStatusFilter
import com.munzenberger.money.presentation.preferences.PreferenceKey
import com.munzenberger.money.presentation.preferences.UserPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/** The filters selected on an account's register. */
@Serializable
data class RegisterFilters(
    val dateFilter: RegisterDateFilter = RegisterDateFilter.AllDates,
    val statusFilter: RegisterStatusFilter = RegisterStatusFilter.AllTransactions,
)

private val registerFiltersKey = PreferenceKey("register.filters", RegisterFilters(), serializer())

/** The filters last selected on the register of the account these preferences are for (see forAccount). */
var UserPreferences.registerFilters: RegisterFilters
    get() = get(registerFiltersKey)
    set(value) = set(registerFiltersKey, value)
