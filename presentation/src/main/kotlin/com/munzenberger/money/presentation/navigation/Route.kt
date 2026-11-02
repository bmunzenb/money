package com.munzenberger.money.presentation.navigation

import com.munzenberger.money.data.api.account.AccountId
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object AccountList : Route

    /** A route to the account form: [NewAccount] adds an account, and [AccountSettings] edits one. */
    @Serializable
    sealed interface AccountFormRoute : Route

    @Serializable
    data object NewAccount : AccountFormRoute

    @Serializable
    data class AccountSettings(val accountId: AccountId) : AccountFormRoute

    @Serializable
    data object CategoryList : Route

    @Serializable
    data object PayeeList : Route

    @Serializable
    data class TransactionList(val accountId: AccountId) : Route
}
