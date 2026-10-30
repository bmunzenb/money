package com.munzenberger.money.desktop.navigation

import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import javafx.beans.value.ObservableValue
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Shows the screen for the top-most [Destination.Screen] in [navigator]'s back stack, and each
 * [Destination.Dialog] above it in a modal window owned by the window below it. Each back stack entry gets
 * its own [ScreenScope] and node, built once by [router] and kept while the entry stays in the stack, so
 * going back shows the same screen and ViewModels. An entry's scope is closed, and its dialog window
 * hidden, when it's popped or replaced, so navigating to its route again starts fresh.
 */
class NavigationHost(
    private val navigator: Navigator,
    private val router: (route: Route, scope: ScreenScope) -> Destination,
) : StackPane(), AutoCloseable {

    private class Entry(val route: Route, val scope: ScreenScope, val destination: Destination) : AutoCloseable {
        // The dialog's window, created the first time it's shown.
        var stage: Stage? = null

        override fun close() {
            stage?.hide()
            scope.close()
        }
    }

    private val entries = mutableListOf<Entry>()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Dialogs need a showing window to own them, so they wait until this host's window is shown.
    private val windowShowing: ObservableValue<Boolean> =
        sceneProperty().flatMap { it.windowProperty() }.flatMap { it.showingProperty() }.orElse(false)

    init {
        windowShowing.addListener { _, _, _ -> showDialogs() }

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

        val screen = entries.lastOrNull { it.destination is Destination.Screen }
        children.setAll(listOfNotNull(screen?.destination?.node))

        showDialogs()
    }

    // Shows the dialogs above the top-most screen, each owned by the window below it, and hides any a
    // screen was pushed over until that screen is popped.
    private fun showDialogs() {
        val screenIndex = entries.indexOfLast { it.destination is Destination.Screen }
        entries.take(screenIndex + 1).forEach { it.stage?.hide() }

        var owner: Window = scene?.window?.takeIf { it.isShowing } ?: return
        entries.drop(screenIndex + 1).forEach { entry ->
            val stage = entry.stage ?: dialogStage(entry, owner).also { entry.stage = it }
            stage.show()
            owner = stage
        }
    }

    private fun dialogStage(entry: Entry, owner: Window) = Stage().apply {
        // Every entry above the top-most screen is a dialog.
        val dialog = entry.destination as Destination.Dialog
        initOwner(owner)
        initModality(Modality.WINDOW_MODAL)
        dialog.title?.let { title = it }
        scene = Scene(dialog.node).apply {
            stylesheets.setAll(owner.scene.stylesheets)
        }
        // Closing the window pops the entry, so the back stack stays what decides which dialogs are open.
        setOnCloseRequest { event ->
            event.consume()
            dismiss(entry)
        }
    }

    // Pops the dialog and anything above it, never below the first route.
    private fun dismiss(entry: Entry) {
        val index = entries.indexOf(entry)
        if (index < 1) return
        navigator.navigate { if (size > index) subList(index, size).clear() }
    }

    override fun close() {
        coroutineScope.cancel()
        entries.forEach { it.close() }
        entries.clear()
        children.clear()
    }
}
