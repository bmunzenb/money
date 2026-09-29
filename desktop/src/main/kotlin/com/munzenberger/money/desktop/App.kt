package com.munzenberger.money.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.navigationRouter
import com.munzenberger.money.desktop.toolbar.AppToolBar
import com.munzenberger.money.shared.theme.MoneyTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: AppViewModel = koinViewModel()) {
    val navigator: Navigator = koinInject()
    val state by viewModel.state.collectAsState()

    MoneyTheme {
        Scaffold(
            topBar = { if (state.isRepositoryConnected) AppToolBar() },
            containerColor = MoneyTheme.colorScheme.background,
        ) { contentPadding ->
            NavDisplay(
                backStack = navigator.backStack,
                entryProvider = navigationRouter,
                modifier = Modifier
                    .background(color = MoneyTheme.colorScheme.background)
                    .fillMaxSize()
                    .padding(contentPadding)
            )
        }
    }
}
