package com.munzenberger.money.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.navigationRouter
import com.munzenberger.money.desktop.rail.AppNavigationRail
import com.munzenberger.money.shared.theme.MoneyTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: AppViewModel = koinViewModel()) {
    val navigator: Navigator = koinInject()
    val state by viewModel.state.collectAsState()

    MoneyTheme {
        Row(
            modifier = Modifier
                .background(color = MoneyTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            if (state.isRepositoryConnected) AppNavigationRail()

            NavDisplay(
                backStack = navigator.backStack,
                entryProvider = navigationRouter,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}
