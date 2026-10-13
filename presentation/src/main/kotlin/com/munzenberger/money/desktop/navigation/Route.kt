package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavKey
import com.munzenberger.money.data.api.account.AccountId
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object AccountList : Route

    @Serializable
    data object NewAccount : Route

    @Serializable
    data class EditAccount(val accountId: AccountId) : Route

    @Serializable
    data object CategoryList : Route

    @Serializable
    data object PayeeList : Route
}
