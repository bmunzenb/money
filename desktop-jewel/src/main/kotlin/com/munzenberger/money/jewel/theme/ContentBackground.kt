package com.munzenberger.money.jewel.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.theme.colorPalette

/**
 * The background of a screen's content, like the IDE's editor: lighter than the panel background around it. White in
 * the light theme and the editor's near-black in the dark theme, from the Int UI gray palette, or the panel background
 * if the palette doesn't have that gray.
 */
val ContentBackground: Color
    @Composable get() {
        val index = if (JewelTheme.isDark) 1 else 14
        return JewelTheme.colorPalette.grayOrNull(index) ?: JewelTheme.globalColors.panelBackground
    }
