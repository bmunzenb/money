package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

typealias NavigationEvent = NavBackStack<Route>.() -> Unit

class Navigator {
    val backStack = NavBackStack<Route>(Route.Welcome)

    private val currentRouteFlow = MutableStateFlow(backStack.lastOrNull())
    val currentRoute: StateFlow<Route?> = currentRouteFlow.asStateFlow()

    fun navigate(event: NavigationEvent) {
        event(backStack)
        currentRouteFlow.value = backStack.lastOrNull()
    }
}
