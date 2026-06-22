package com.munzenberger.money.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.munzenberger.money.shared.theme.MoneyTheme

@Composable
fun App() {
    MoneyTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize()
        ) {
            Text(
                text = "Welcome to Money!",
                style = MoneyTheme.typography.titleLarge
            )
        }
    }
}
