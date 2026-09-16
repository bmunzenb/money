package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.munzenberger.money.desktop.accountlist.AccountListScreen
import com.munzenberger.money.desktop.welcome.WelcomeScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object AccountList : Route
}

val navigationRouter = entryProvider<Route> {
    entry<Route.Welcome> {
        WelcomeScreen()
    }

    entry<Route.AccountList> {
        AccountListScreen()
    }
}
