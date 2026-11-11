package com.munzenberger.money.desktop.navigation

import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import javafx.scene.Parent
import javafx.scene.layout.StackPane
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

    // An entry has a dialog if its destination is a Destination.Dialog, and is shown in the pane otherwise.
    private class Entry(
        val route: Route,
        val scope: ScreenScope,
        val node: Parent,
        val dialog: ScreenDialog?,
    ) : AutoCloseable {
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
            entries += entry(route, scope, router(route, scope))
        }

        // The pane shows the top-most screen. The dialogs above it are shown, and the ones below it are hidden until
        // going back shows them again.
        val screenIndex = entries.indexOfLast { it.dialog == null }
        children.setAll(listOfNotNull(entries.getOrNull(screenIndex)?.node))
        entries.forEachIndexed { index, entry ->
            if (index > screenIndex) entry.dialog?.show() else entry.dialog?.hide()
        }
    }

    // A dialog is owned by the window below it: the entry below's dialog, or the host's window if that's a screen. The
    // owner never changes, since an entry is kept only while the entries below it are. Unless the dialog handles it,
    // closing its window goes back from its route, which pops the entry and closes the dialog.
    private fun entry(route: Route, scope: ScreenScope, destination: Destination): Entry {
        val dialog = (destination as? Destination.Dialog)?.let {
            ScreenDialog(
                destination = it,
                owner = entries.lastOrNull()?.dialog?.window ?: scene?.window,
                onCloseRequest = it.onCloseRequest ?: { navigator.back(from = route) },
            )
        }
        return Entry(route, scope, destination.node, dialog)
    }

    override fun close() {
        coroutineScope.cancel()
        entries.forEach { it.close() }
        entries.clear()
        children.clear()
    }
}
