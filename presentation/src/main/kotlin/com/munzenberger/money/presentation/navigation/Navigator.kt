package com.munzenberger.money.presentation.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

typealias NavigationEvent = MutableList<Route>.() -> Unit

class Navigator {
    private val backStackFlow = MutableStateFlow<List<Route>>(listOf(Route.Welcome))
    val backStack: StateFlow<List<Route>> = backStackFlow.asStateFlow()

    private val currentRouteFlow = MutableStateFlow<Route?>(backStackFlow.value.lastOrNull())
    val currentRoute: StateFlow<Route?> = currentRouteFlow.asStateFlow()

    fun navigate(event: NavigationEvent) {
        val backStack = backStackFlow.updateAndGet { it.toMutableList().apply(event).toList() }
        currentRouteFlow.value = backStack.lastOrNull()
    }

    /**
     * Pops the top route, but only if it's [from] (or any route, if [from] is null), so a screen that goes back
     * after the user has already left it doesn't pop another screen. Never pops the last route, so there's always
     * a screen to show.
     */
    fun back(from: Route? = null) {
        navigate { if (size > 1 && (from == null || last() == from)) removeLast() }
    }
}
