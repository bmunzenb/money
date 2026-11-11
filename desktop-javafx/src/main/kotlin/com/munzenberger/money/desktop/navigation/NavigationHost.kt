package com.munzenberger.money.desktop.navigation

import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import javafx.scene.layout.StackPane
import javafx.stage.Window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Shows the screen for the top of [navigator]'s back stack. Each back stack entry gets its own
 * [ScreenScope] and [Destination], built once by [router] and kept while the entry stays in the stack, so going
 * back shows the same screen and ViewModels. An entry's scope is closed when it's popped or replaced,
 * so navigating to its route again starts fresh.
 *
 * The pane shows the top-most [Destination.Screen], and each [Destination.Dialog] above it is shown in a
 * [ScreenDialog] owned by the window below it, so dialogs can open over dialogs. Dialogs below the top-most screen
 * are hidden until going back shows them again.
 */
class NavigationHost(
    private val navigator: Navigator,
    private val router: (route: Route, scope: ScreenScope) -> Destination,
) : StackPane(), AutoCloseable {

    private class Entry(val route: Route, val scope: ScreenScope, val destination: Destination) : AutoCloseable {
        // Created the first time a dialog entry is shown.
        var dialog: ScreenDialog? = null

        override fun close() {
            dialog?.close()
            scope.close()
        }
    }

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
        entries.drop(kept).forEach { it.close() }
        entries.subList(kept, entries.size).clear()

        backStack.drop(kept).forEach { route ->
            val scope = ScreenScope()
            entries += Entry(route, scope, router(route, scope))
        }

        val screenIndex = entries.indexOfLast { it.destination is Destination.Screen }
        children.setAll(listOfNotNull(entries.getOrNull(screenIndex)?.destination?.node))

        entries.take(screenIndex.coerceAtLeast(0)).forEach { it.dialog?.hide() }

        var owner: Window? = scene?.window
        entries.drop(screenIndex + 1).forEach { entry ->
            val dialog = entry.dialog ?: entry.createDialog(owner).also { entry.dialog = it }
            dialog.show()
            owner = dialog.window
        }
    }

    // Unless the dialog handles it, closing its window goes back from its route, which pops the entry and closes the
    // dialog.
    private fun Entry.createDialog(owner: Window?): ScreenDialog {
        val dialog = destination as Destination.Dialog
        return ScreenDialog(
            destination = dialog,
            owner = owner,
            onCloseRequest = dialog.onCloseRequest ?: { navigator.back(from = route) },
        )
    }

    override fun close() {
        coroutineScope.cancel()
        entries.forEach { it.close() }
        entries.clear()
        children.clear()
    }
}
