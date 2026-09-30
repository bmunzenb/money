package com.munzenberger.money.desktop.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.desktop.database.rememberCreateDatabaseLauncher
import com.munzenberger.money.desktop.database.rememberOpenDatabaseLauncher
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.create_database_button_title
import money.shared.generated.resources.create_database_error_message
import money.shared.generated.resources.open_database_button_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val createDatabase = rememberCreateDatabaseLauncher(onFileSelected = viewModel::createDatabase)
    val openDatabase = rememberOpenDatabaseLauncher(onFileSelected = viewModel::openDatabase)

    WelcomeScreenContent(
        state = state,
        onCreateDatabaseClick = createDatabase,
        onOpenDatabaseClick = openDatabase
    )
}

@Composable
private fun WelcomeScreenContent(
    state: WelcomeUiState,
    onCreateDatabaseClick: () -> Unit,
    onOpenDatabaseClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state is WelcomeUiState.Loading) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.Start) {
                TextButton(onClick = onCreateDatabaseClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(Res.string.create_database_button_title))
                }

                TextButton(onClick = onOpenDatabaseClick) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(Res.string.open_database_button_title))
                }
            }

            if (state is WelcomeUiState.Error) {
                Text(
                    text = stringResource(state.messageRes),
                    color = MoneyTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    PreviewThemed {
        WelcomeScreenContent(
            state = WelcomeUiState.Idle,
            onCreateDatabaseClick = {},
            onOpenDatabaseClick = {}
        )
    }
}

@Preview
@Composable
private fun WelcomeScreenLoadingPreview() {
    PreviewThemed {
        WelcomeScreenContent(
            state = WelcomeUiState.Loading,
            onCreateDatabaseClick = {},
            onOpenDatabaseClick = {}
        )
    }
}

@Preview
@Composable
private fun WelcomeScreenErrorPreview() {
    PreviewThemed {
        WelcomeScreenContent(
            state = WelcomeUiState.Error(Res.string.create_database_error_message),
            onCreateDatabaseClick = {},
            onOpenDatabaseClick = {}
        )
    }
}
