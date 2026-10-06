package com.munzenberger.money.desktop.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Sizes shared by forms, such as the New account screen. */
object FormDefaults {
    /**
     * The widest a form gets. Forms are capped at this width and start-aligned, so on wide windows their
     * fields stay a comfortable width and line up with the screen header instead of stretching.
     */
    val MaxWidth: Dp = 600.dp
}
