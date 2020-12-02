package com.munzenberger.money.app.control

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.scene.Cursor
import javafx.stage.Stage

private fun Stage.setWaiting(waiting: Boolean) {
    scene.cursor = when (waiting) {
        true -> Cursor.WAIT
        else -> Cursor.DEFAULT
    }
    scene.root.isDisable = waiting
}

fun Stage.bindWaiting(booleanProperty: ReadOnlyBooleanProperty) {
    booleanProperty.addListener { _, _, newValue ->
        setWaiting(newValue)
    }
}
