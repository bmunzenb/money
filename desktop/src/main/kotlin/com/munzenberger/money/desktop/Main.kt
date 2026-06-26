package com.munzenberger.money.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import money.shared.generated.resources.Res
import money.shared.generated.resources.app_title
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_title),
    ) {
        App()
    }
}
