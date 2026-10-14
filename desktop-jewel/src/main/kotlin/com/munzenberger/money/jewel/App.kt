package com.munzenberger.money.jewel

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.munzenberger.money.jewel.navigation.navigationRouter
import com.munzenberger.money.jewel.rail.AppNavigationRail
import com.munzenberger.money.presentation.AppViewModel
import com.munzenberger.money.presentation.navigation.Navigator
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: AppViewModel = koinViewModel()) {
    val navigator: Navigator = koinInject()
    val state by viewModel.state.collectAsState()

    IntUiTheme(isDark = isSystemInDarkTheme()) {
        Row(
            modifier = Modifier
                .background(color = JewelTheme.globalColors.panelBackground)
                .fillMaxSize()
        ) {
            if (state.isRepositoryConnected) {
                AppNavigationRail()
                Divider(orientation = Orientation.Vertical, modifier = Modifier.fillMaxHeight())
            }

            NavDisplay(
                backStack = navigator.backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = navigationRouter,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}
