package com.munzenberger.money.desktop

import com.munzenberger.money.desktop.menu.AppMenuBar
import com.munzenberger.money.desktop.navigation.NavigationHost
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.navigation.navigationRouter
import com.munzenberger.money.desktop.rail.AppNavigationRail
import com.munzenberger.money.desktop.toolbar.AppToolbar
import com.munzenberger.money.presentation.AppUiState
import com.munzenberger.money.presentation.AppViewModel
import com.munzenberger.money.presentation.navigation.Navigator
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The root of the scene: shows the [NavigationHost] for the back stack below the [AppMenuBar], with the
 * [AppToolbar] above it and the [AppNavigationRail] beside it while a database is open. [scope] is app-lifetime
 * and holds the [AppViewModel], which navigates as the open repository changes, and the menu bar's, toolbar's
 * and rail's ViewModels.
 */
class AppScreen(scope: ScreenScope) : BorderPane(), KoinComponent, AutoCloseable {
    private val navigator: Navigator by inject()

    private val viewModel: AppViewModel = scope.viewModel()

    private val navigationHost = NavigationHost(navigator, navigationRouter)

    private val menuBar = AppMenuBar(scope)

    private val toolbar = AppToolbar(scope)

    // Where the OS has a system menu bar, as on macOS, the menu bar moves there and takes no space here.
    private val header = VBox(menuBar)

    private val navigationRail = AppNavigationRail(scope)

    init {
        top = header
        center = navigationHost
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AppUiState) {
        header.children.setAll(listOfNotNull(menuBar, toolbar.takeIf { state.isRepositoryConnected }))
        left = if (state.isRepositoryConnected) navigationRail else null
    }

    override fun close() {
        navigationHost.close()
    }
}
