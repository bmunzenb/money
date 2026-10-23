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
}
