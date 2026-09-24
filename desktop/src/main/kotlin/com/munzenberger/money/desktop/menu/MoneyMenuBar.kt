package com.munzenberger.money.desktop.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.munzenberger.money.desktop.database.rememberCreateDatabaseLauncher
import com.munzenberger.money.desktop.database.rememberOpenDatabaseLauncher
import money.shared.generated.resources.Res
import money.shared.generated.resources.close_database_menu_item_title
import money.shared.generated.resources.exit_menu_item_title
import money.shared.generated.resources.file_menu_title
import money.shared.generated.resources.new_database_menu_item_title
import money.shared.generated.resources.open_database_menu_item_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import java.util.Locale

private val isWindows: Boolean
    get() = System.getProperty("os.name")?.lowercase(Locale.ENGLISH)?.contains("windows") == true

@Composable
fun FrameWindowScope.MoneyMenuBar(
    onExit: () -> Unit,
    viewModel: MenuBarViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    MenuBar {
        Menu(stringResource(Res.string.file_menu_title)) {
            val createDatabase = rememberCreateDatabaseLauncher(
                onFileSelected = viewModel::onNewDatabaseSelected,
            )
            val openDatabase = rememberOpenDatabaseLauncher(
                onFileSelected = viewModel::onOpenDatabaseSelected,
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
                enabled = state.closeRepositoryEnabled,
                onClick = viewModel::onCloseDatabaseSelected,
            )
            if (isWindows) {
                Separator()
                Item(
                    stringResource(Res.string.exit_menu_item_title),
                    shortcut = KeyShortcut(Key.Q, ctrl = true),
                    onClick = {
                        viewModel.onExitSelected()
                        onExit()
                    },
                )
            }
        }
    }
}
