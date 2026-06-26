package com.munzenberger.money.desktop.welcome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.munzenberger.money.shared.theme.MoneyTheme
import money.shared.generated.resources.Res
import money.shared.generated.resources.welcome_message
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .safeContentPadding()
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(Res.string.welcome_message),
            style = MoneyTheme.typography.titleLarge
        )
    }
}
