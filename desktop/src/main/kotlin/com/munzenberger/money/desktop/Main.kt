package com.munzenberger.money.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.munzenberger.money.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Money",
    ) {
        App()
    }
}
