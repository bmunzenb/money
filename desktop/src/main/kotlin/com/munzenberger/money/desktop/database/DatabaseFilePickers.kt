package com.munzenberger.money.desktop.database

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.LocalAwtWindow
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import money.shared.generated.resources.Res
import money.shared.generated.resources.create_database_dialog_title
import money.shared.generated.resources.default_file_name
import money.shared.generated.resources.open_database_dialog_title
import org.jetbrains.compose.resources.stringResource
import java.io.File

private const val MONEY_DATABASE_FILE_EXTENSION = "mdb"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberCreateDatabaseLauncher(onFileSelected: (File) -> Unit): () -> Unit {
    val window = LocalAwtWindow.current
    val suggestedName = stringResource(Res.string.default_file_name)

    val launcher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings(
            title = stringResource(Res.string.create_database_dialog_title),
            parent = window?.let { FileKitDialogParent.awt(it) }
        )
    ) { file ->
        file?.let { onFileSelected(it.file) }
    }

    return { launcher.launch(suggestedName = suggestedName, defaultExtension = MONEY_DATABASE_FILE_EXTENSION) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberOpenDatabaseLauncher(onFileSelected: (File) -> Unit): () -> Unit {
    val window = LocalAwtWindow.current

    val launcher = rememberFilePickerLauncher(
        dialogSettings = FileKitDialogSettings(
            title = stringResource(Res.string.open_database_dialog_title),
            parent = window?.let { FileKitDialogParent.awt(it) }
        )
    ) { file ->
        file?.let { onFileSelected(it.file) }
    }

    return { launcher.launch() }
}
