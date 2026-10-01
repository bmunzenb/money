package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.munzenberger.money.desktop.accounts.AccountListScreen
import com.munzenberger.money.desktop.accounts.NewAccountScreen
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
        NewAccountScreen()
    }

    entry<Route.CategoryList> {
        CategoryListScreen()
    }

    entry<Route.PayeeList> {
        PayeeListScreen()
    }
}
