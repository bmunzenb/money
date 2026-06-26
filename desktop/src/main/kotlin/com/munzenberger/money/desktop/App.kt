package com.munzenberger.money.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.ui.NavDisplay
import com.munzenberger.money.desktop.navigation.Route
import com.munzenberger.money.desktop.navigation.navigationRouter
import com.munzenberger.money.shared.theme.MoneyTheme

@Composable
fun App() {
    MoneyTheme {
        val backStack = remember { NavBackStack(Route.Welcome) }
        NavDisplay(
            backStack = backStack,
            entryProvider = navigationRouter,
            modifier = Modifier
                .background(color = MoneyTheme.colorScheme.background)
                .fillMaxSize()
        )
    }
}
