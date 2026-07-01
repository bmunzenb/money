package com.munzenberger.money.desktop.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import money.shared.generated.resources.Res
import money.shared.generated.resources.create_database_button_title
import money.shared.generated.resources.open_database_button_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("detekt:UnusedParameter")
fun WelcomeScreen(viewModel: WelcomeViewModel = koinViewModel()) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {}
        ) {
            Text(
                text = stringResource(Res.string.create_database_button_title)
            )
        }
        Button(
            onClick = {}
        ) {
            Text(
                text = stringResource(Res.string.open_database_button_title)
            )
        }
    }
}
