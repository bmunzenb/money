package com.munzenberger.money.app.control

import javafx.scene.Cursor
import javafx.stage.Stage

fun Stage.setWaiting(waiting: Boolean) {
    scene.cursor = when (waiting) {
        true -> Cursor.WAIT
        else -> Cursor.DEFAULT
    }
    scene.root.isDisable = waiting
}
