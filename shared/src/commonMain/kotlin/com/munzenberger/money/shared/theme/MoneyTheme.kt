package com.munzenberger.money.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalMoneyTheme = staticCompositionLocalOf { MoneyTheme() }

@Composable
fun MoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val moneyTheme = MoneyTheme(
        colorScheme = if (darkTheme) darkColorScheme else lightColorScheme,
        typography = typography,
        shapes = shapes,
        spacing = spacing,
    )

    CompositionLocalProvider(LocalMoneyTheme provides moneyTheme) {
        MaterialTheme(
            colorScheme = moneyTheme.colorScheme,
            shapes = moneyTheme.shapes,
            typography = moneyTheme.typography,
            content = content
        )
    }
}

data class MoneyTheme(
    val colorScheme: ColorScheme = lightColorScheme(),
    val typography: Typography = Typography(),
    val shapes: Shapes = Shapes(),
    val spacing: Spacing = Spacing(),
) {
    companion object {
        val colorScheme: ColorScheme
            @Composable
            @ReadOnlyComposable
            get() = LocalMoneyTheme.current.colorScheme

        val typography: Typography
            @Composable
            @ReadOnlyComposable
            get() = LocalMoneyTheme.current.typography

        val shapes: Shapes
            @Composable
            @ReadOnlyComposable
            get() = LocalMoneyTheme.current.shapes

        val spacing: Spacing
            @Composable
            @ReadOnlyComposable
            get() = LocalMoneyTheme.current.spacing
    }
}
