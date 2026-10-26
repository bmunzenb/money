package com.munzenberger.money.desktop

import com.munzenberger.money.desktop.navigation.NavigationHost
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.navigation.navigationRouter
import com.munzenberger.money.desktop.rail.AppNavigationRail
import com.munzenberger.money.desktop.toolbar.AppToolbar
import com.munzenberger.money.presentation.AppUiState
import com.munzenberger.money.presentation.AppViewModel
import com.munzenberger.money.presentation.navigation.Navigator
import javafx.scene.layout.BorderPane
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The root of the scene: shows the [NavigationHost] for the back stack, with the [AppToolbar] above it and
 * the [AppNavigationRail] beside it while a database is open. [scope] is app-lifetime and holds the
 * [AppViewModel], which navigates as the open repository changes, and the toolbar's and rail's ViewModels.
 */
class AppScreen(scope: ScreenScope) : BorderPane(), KoinComponent, AutoCloseable {
    private val navigator: Navigator by inject()

    private val viewModel: AppViewModel = scope.viewModel()

    private val navigationHost = NavigationHost(navigator, navigationRouter)

    private val toolbar = AppToolbar(scope)

    private val navigationRail = AppNavigationRail(scope)

    init {
        center = navigationHost
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AppUiState) {
        top = if (state.isRepositoryConnected) toolbar else null
        left = if (state.isRepositoryConnected) navigationRail else null
    }

    override fun close() {
        navigationHost.close()
    }
}
