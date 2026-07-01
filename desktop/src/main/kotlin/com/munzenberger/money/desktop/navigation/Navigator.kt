package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class NavigationEvent(val block: NavBackStack<Route>.() -> Unit)

class Navigator {
    private val eventsFlow = MutableSharedFlow<NavigationEvent>()
    val events = eventsFlow.asSharedFlow()

    suspend fun navigate(event: NavigationEvent) {
        eventsFlow.emit(event)
    }

    suspend fun navigate(block: NavBackStack<Route>.() -> Unit) {
        navigate(NavigationEvent(block))
    }
}
