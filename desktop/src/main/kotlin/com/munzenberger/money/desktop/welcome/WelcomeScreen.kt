package com.munzenberger.money.desktop.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.desktop.database.rememberCreateDatabaseLauncher
import com.munzenberger.money.desktop.database.rememberOpenDatabaseLauncher
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.create_database_button_title
import money.shared.generated.resources.open_database_button_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel = koinViewModel()) {
    val createDatabase = rememberCreateDatabaseLauncher(onFileSelected = viewModel::createDatabase)
    val openDatabase = rememberOpenDatabaseLauncher(onFileSelected = viewModel::openDatabase)

    WelcomeScreenContent(
        onCreateDatabaseClick = createDatabase,
        onOpenDatabaseClick = openDatabase
    )
}

@Composable
private fun WelcomeScreenContent(
    onCreateDatabaseClick: () -> Unit,
    onOpenDatabaseClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = onCreateDatabaseClick) {
            Text(text = stringResource(Res.string.create_database_button_title))
        }

        Button(onClick = onOpenDatabaseClick) {
            Text(text = stringResource(Res.string.open_database_button_title))
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    PreviewThemed {
        WelcomeScreenContent(
            onCreateDatabaseClick = {},
            onOpenDatabaseClick = {}
        )
    }
}
