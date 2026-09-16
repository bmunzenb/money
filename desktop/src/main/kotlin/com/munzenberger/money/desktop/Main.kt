package com.munzenberger.money.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.munzenberger.money.desktop.inject.appModule
import money.shared.generated.resources.Res
import money.shared.generated.resources.app_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_title),
    ) {
        KoinApplication(configuration = koinConfiguration {
            modules(appModule)
        }) {
            App()
        }
    }
}
