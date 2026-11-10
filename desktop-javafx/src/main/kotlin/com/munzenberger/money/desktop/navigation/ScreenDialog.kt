package com.munzenberger.money.desktop.navigation

import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

/**
 * The modal window of a [Destination.Dialog] back stack entry, owned by [owner] and styled like it. Closing the
 * window calls [onCloseRequest] to go back rather than hiding it, so only the back stack opens and closes dialogs:
 * [close] hides the window when the entry is popped.
 */
class ScreenDialog(
    destination: Destination.Dialog,
    owner: Window?,
    onCloseRequest: () -> Unit,
) : AutoCloseable {
    private val stage = Stage().apply {
        owner?.let(::initOwner)
        initModality(Modality.WINDOW_MODAL)
        title = destination.title
        scene = Scene(destination.node).apply {
            owner?.scene?.let { stylesheets.setAll(it.stylesheets) }
        }
        setOnCloseRequest { event ->
            event.consume()
            onCloseRequest()
        }
    }

    /** The dialog's window, to own the dialogs shown over it. */
    val window: Window get() = stage

    // Not showAndWait(), which would block the back stack collector that shows it until the window closes.
    fun show() = stage.show()

    // Hiding the window doesn't fire its close request, so it doesn't go back again.
    fun hide() = stage.hide()

    override fun close() = hide()
}
