package com.munzenberger.money.desktop

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.munzenberger.money.desktop.database.rememberCreateDatabaseLauncher
import com.munzenberger.money.desktop.database.rememberOpenDatabaseLauncher
import com.munzenberger.money.desktop.inject.appModule
import money.shared.generated.resources.Res
import money.shared.generated.resources.app_title
import money.shared.generated.resources.close_database_menu_item_title
import money.shared.generated.resources.exit_menu_item_title
import money.shared.generated.resources.file_menu_title
import money.shared.generated.resources.new_database_menu_item_title
import money.shared.generated.resources.open_database_menu_item_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import java.util.Locale

private val isWindows: Boolean
    get() = System.getProperty("os.name")?.lowercase(Locale.ENGLISH)?.contains("windows") == true

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        val menuBarViewModel: MenuBarViewModel = koinInject()
        val menuBarState by menuBarViewModel.state.collectAsState()

        val shutdown: () -> Unit = {
            menuBarViewModel.onExitSelected()
            exitApplication()
        }

        Window(
            onCloseRequest = shutdown,
            title = stringResource(Res.string.app_title),
        ) {
            MenuBar {
                Menu(stringResource(Res.string.file_menu_title)) {
                    val createDatabase = rememberCreateDatabaseLauncher(
                        onFileSelected = menuBarViewModel::onNewDatabaseSelected,
                    )
                    val openDatabase = rememberOpenDatabaseLauncher(
                        onFileSelected = menuBarViewModel::onOpenDatabaseSelected,
                    )

                    Item(
                        stringResource(Res.string.new_database_menu_item_title),
                        onClick = createDatabase,
                    )
                    Item(
                        stringResource(Res.string.open_database_menu_item_title),
                        onClick = openDatabase,
                    )
                    Item(
                        stringResource(Res.string.close_database_menu_item_title),
                        enabled = menuBarState.closeRepositoryEnabled,
                        onClick = menuBarViewModel::onCloseDatabaseSelected,
                    )
                    if (isWindows) {
                        Separator()
                        Item(
                            stringResource(Res.string.exit_menu_item_title),
                            shortcut = KeyShortcut(Key.Q, ctrl = true),
                            onClick = shutdown,
                        )
                    }
                }
            }

            App()
        }
    }
}
