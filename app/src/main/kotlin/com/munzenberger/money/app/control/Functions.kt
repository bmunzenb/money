package com.munzenberger.money.app.control

import javafx.scene.Cursor

fun booleanToWaitCursor(input: Boolean): Cursor =
        when (input) {
            true -> Cursor.WAIT
            else -> Cursor.DEFAULT
        }
