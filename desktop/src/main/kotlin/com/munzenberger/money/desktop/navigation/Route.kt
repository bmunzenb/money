package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.desktop.accounts.AccountListScreen
import com.munzenberger.money.desktop.accounts.AccountFormScreen
import com.munzenberger.money.desktop.categories.CategoryListScreen
import com.munzenberger.money.desktop.payees.PayeeListScreen
import com.munzenberger.money.desktop.welcome.WelcomeScreen
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

val navigationRouter = entryProvider<Route> {
    entry<Route.Welcome> {
        WelcomeScreen()
    }

    entry<Route.AccountList> {
        AccountListScreen()
    }

    entry<Route.NewAccount> {
        AccountFormScreen()
    }

    entry<Route.EditAccount> {
        AccountFormScreen(accountId = it.accountId)
    }

    entry<Route.CategoryList> {
        CategoryListScreen()
    }

    entry<Route.PayeeList> {
        PayeeListScreen()
    }
}
