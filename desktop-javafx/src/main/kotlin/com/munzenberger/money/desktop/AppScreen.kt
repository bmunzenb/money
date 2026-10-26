package com.munzenberger.money.desktop

import com.munzenberger.money.desktop.navigation.NavigationHost
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.navigation.navigationRouter
import com.munzenberger.money.presentation.AppViewModel
import com.munzenberger.money.presentation.navigation.Navigator
import javafx.scene.layout.BorderPane
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The root of the scene: shows the [NavigationHost] for the back stack. [scope] is app-lifetime and holds
 * the [AppViewModel], which navigates as the open repository changes.
 */
class AppScreen(scope: ScreenScope) : BorderPane(), KoinComponent, AutoCloseable {
    private val navigator: Navigator by inject()

    private val navigationHost = NavigationHost(navigator, navigationRouter)

    init {
        scope.viewModel<AppViewModel>()
        center = navigationHost
    }

    override fun close() {
        navigationHost.close()
    }
}
