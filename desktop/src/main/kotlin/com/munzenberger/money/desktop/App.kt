package com.munzenberger.money.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.ui.NavDisplay
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import com.munzenberger.money.desktop.navigation.navigationRouter
import com.munzenberger.money.shared.theme.MoneyTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val navigator: Navigator = koinInject()
    val backStack = remember { NavBackStack<Route>(Route.Welcome) }

    LaunchedEffect(Unit) {
        navigator.events.collect { event ->
            event.block(backStack)
        }
    }

    MoneyTheme {
        NavDisplay(
            backStack = backStack,
            entryProvider = navigationRouter,
            modifier = Modifier
                .background(color = MoneyTheme.colorScheme.background)
                .fillMaxSize()
        )
    }
}
