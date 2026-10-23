package com.munzenberger.money.jewel.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.munzenberger.money.jewel.theme.PreviewThemed
import com.munzenberger.money.presentation.database.rememberCreateDatabaseLauncher
import com.munzenberger.money.presentation.database.rememberOpenDatabaseLauncher
import com.munzenberger.money.presentation.welcome.WelcomeUiState
import com.munzenberger.money.presentation.welcome.WelcomeViewModel
import money.presentation.generated.resources.Res
import money.presentation.generated.resources.create_database_button_title
import money.presentation.generated.resources.create_database_error_message
import money.presentation.generated.resources.open_database_button_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
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
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state is WelcomeUiState.Loading) {
            CircularProgressIndicatorBig()
        } else {
            // The buttons share the width of the wider one.
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(IntrinsicSize.Max),
            ) {
                WelcomeButton(
                    text = stringResource(Res.string.create_database_button_title),
                    onClick = onCreateDatabaseClick,
                )

                WelcomeButton(
                    text = stringResource(Res.string.open_database_button_title),
                    onClick = onOpenDatabaseClick,
                )
            }

            if (state is WelcomeUiState.Error) {
                Text(
                    text = stringResource(state.messageRes),
                    color = JewelTheme.globalColors.text.error
                )
            }
        }
    }
}

@Composable
private fun WelcomeButton(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = text)
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
