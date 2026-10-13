package com.munzenberger.money.jewel

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.munzenberger.money.jewel.navigation.navigationRouter
import com.munzenberger.money.presentation.AppViewModel
import com.munzenberger.money.presentation.navigation.Navigator
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    // Not read yet, but creating it starts the app-wide navigation between Welcome and the accounts.
    @Suppress("UnusedParameter") viewModel: AppViewModel = koinViewModel(),
) {
    val navigator: Navigator = koinInject()

    IntUiTheme(isDark = isSystemInDarkTheme()) {
        NavDisplay(
            backStack = navigator.backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = navigationRouter,
            modifier = Modifier
                .background(color = JewelTheme.globalColors.panelBackground)
                .fillMaxSize(),
        )
    }
}
