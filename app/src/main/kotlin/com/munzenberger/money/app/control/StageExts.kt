package com.munzenberger.money.app.control

import javafx.scene.Cursor
import javafx.scene.Scene

@Deprecated("Bind to the disable and cursor properties directly")
fun Scene.setWaiting(waiting: Boolean, defaultCursor: Cursor = Cursor.DEFAULT) {
    cursor = when (waiting) {
        true -> Cursor.WAIT
        else -> defaultCursor
    }
    root.isDisable = waiting
}
