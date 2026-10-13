package com.munzenberger.money.jewel.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme

/** Wraps preview content in the app's Jewel theme, on a full-size box filled with the panel background. */
@Composable
fun PreviewThemed(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    IntUiTheme(isDark = darkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = JewelTheme.globalColors.panelBackground)
        ) {
            content()
        }
    }
}
