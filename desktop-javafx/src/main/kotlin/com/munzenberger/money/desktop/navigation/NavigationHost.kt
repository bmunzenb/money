package com.munzenberger.money.desktop.navigation

import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import javafx.scene.Node
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Shows the screen for the top of [navigator]'s back stack. Each back stack entry gets its own
 * [ScreenScope] and node, built once by [router] and kept while the entry stays in the stack, so going
 * back shows the same screen and ViewModels. An entry's scope is closed when it's popped or replaced, so
 * navigating to its route again starts fresh.
 */
class NavigationHost(
    navigator: Navigator,
    private val router: (route: Route, scope: ScreenScope) -> Node,
) : StackPane(), AutoCloseable {

    private class Entry(val route: Route, val scope: ScreenScope, val node: Node)

    private val entries = mutableListOf<Entry>()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        coroutineScope.launch {
            navigator.backStack.collect(::show)
        }
    }

    private fun show(backStack: List<Route>) {
        // Keeps the entries up to the first route that changed, and closes the rest.
        val kept = entries.zip(backStack).takeWhile { (entry, route) -> entry.route == route }.size
        entries.drop(kept).forEach { it.scope.close() }
        entries.subList(kept, entries.size).clear()

        backStack.drop(kept).forEach { route ->
            val scope = ScreenScope()
            entries += Entry(route, scope, router(route, scope))
        }

        children.setAll(listOfNotNull(entries.lastOrNull()?.node))
    }

    override fun close() {
        coroutineScope.cancel()
        entries.forEach { it.scope.close() }
        entries.clear()
        children.clear()
    }
}
