package com.munzenberger.money.desktop.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.LocalAwtWindow
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import money.shared.generated.resources.Res
import money.shared.generated.resources.create_database_button_title
import money.shared.generated.resources.create_database_dialog_title
import money.shared.generated.resources.default_file_name
import money.shared.generated.resources.open_database_button_title
import money.shared.generated.resources.open_database_dialog_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel = koinViewModel()) {
    val window = LocalAwtWindow.current
    val defaultFileName = stringResource(Res.string.default_file_name)

    val createDatabaseLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings(
            title = stringResource(Res.string.create_database_dialog_title),
            parentWindow = window
        )
    ) { file ->
        file?.let { viewModel.createDatabase(it.file) }
    }

    val openDatabaseLauncher = rememberFilePickerLauncher(
        dialogSettings = FileKitDialogSettings(
            title = stringResource(Res.string.open_database_dialog_title),
            parentWindow = window
        )
    ) { file ->
        file?.let { viewModel.openDatabase(it.file) }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = { createDatabaseLauncher.launch(suggestedName = defaultFileName, defaultExtension = "mdb") }
        ) {
            Text(text = stringResource(Res.string.create_database_button_title))
        }

        Button(
            onClick = { openDatabaseLauncher.launch() }
        ) {
            Text(text = stringResource(Res.string.open_database_button_title))
        }
    }
}
