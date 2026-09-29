package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

typealias NavigationEvent = NavBackStack<Route>.() -> Unit

class Navigator {
    val backStack = NavBackStack<Route>(Route.Welcome)

    private val canNavigateBackFlow = MutableStateFlow(backStack.size > 1)
    val canNavigateBack: StateFlow<Boolean> = canNavigateBackFlow.asStateFlow()

    fun navigate(event: NavigationEvent) {
        event(backStack)
        canNavigateBackFlow.value = backStack.size > 1
    }
}
