package com.munzenberger.money.desktop

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.munzenberger.money.desktop.inject.appModule
import money.shared.generated.resources.Res
import money.shared.generated.resources.app_title
import money.shared.generated.resources.exit_menu_item_title
import money.shared.generated.resources.file_menu_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import java.util.Locale

private val isWindows: Boolean
    get() = System.getProperty("os.name")?.lowercase(Locale.ENGLISH)?.contains("windows") == true

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_title),
    ) {
        MenuBar {
            Menu(stringResource(Res.string.file_menu_title)) {
                if (isWindows) {
                    Item(
                        stringResource(Res.string.exit_menu_item_title),
                        shortcut = KeyShortcut(Key.Q, ctrl = true),
                        onClick = ::exitApplication,
                    )
                }
            }
        }

        KoinApplication(configuration = koinConfiguration {
            modules(appModule)
        }) {
            App()
        }
    }
}
