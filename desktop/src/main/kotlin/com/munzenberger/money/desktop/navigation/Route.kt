package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.munzenberger.money.desktop.welcome.WelcomeScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Welcome : Route
}

val navigationRouter = entryProvider {
    entry<Route.Welcome> {
        WelcomeScreen()
    }
}
