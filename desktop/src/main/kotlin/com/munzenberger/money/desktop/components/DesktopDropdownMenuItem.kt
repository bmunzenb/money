package com.munzenberger.money.desktop.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed

/**
 * A Material 3 dropdown menu item sized for a desktop app used with a mouse and keyboard, to go with
 * [DesktopOutlinedTextField]. Rows are 32dp tall instead of 48dp, and the text lines up with the text in a
 * [DesktopOutlinedTextField] that anchors the menu (see [DesktopDropdownMenuItemDefaults]).
 *
 * [DropdownMenuItem] applies its 48dp minimum height inside its own modifier chain, after [modifier], so a
 * fixed height here takes precedence over it. Because the height is fixed, [text] is shown on a single
 * line, ending with an ellipsis if it doesn't fit.
 */
@Composable
fun DesktopDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    interactionSource: MutableInteractionSource? = null,
) {
    DropdownMenuItem(
        text = { Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        onClick = onClick,
        modifier = modifier.height(DesktopDropdownMenuItemDefaults.Height),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = DesktopDropdownMenuItemDefaults.ContentPadding,
        interactionSource = interactionSource,
    )
}

/** Sizes used by [DesktopDropdownMenuItem]. */
object DesktopDropdownMenuItemDefaults {
    /** Height of each row. Material's minimum is 48dp. */
    val Height: Dp = 32.dp

    /**
     * Padding at the start and end of each row. It matches [DesktopOutlinedTextFieldDefaults.ContentPadding],
     * so an option's text lines up with the text in the field that anchors the menu.
     */
    val ContentPadding: PaddingValues = PaddingValues(horizontal = 12.dp)
}

@Preview
@Composable
private fun DesktopDropdownMenuItemPreview() {
    PreviewThemed {
        Surface(
            modifier = Modifier.padding(MoneyTheme.spacing.medium),
            shape = MenuDefaults.shape,
            color = MenuDefaults.containerColor,
            tonalElevation = MenuDefaults.TonalElevation,
            shadowElevation = MenuDefaults.ShadowElevation,
        ) {
            Column(modifier = Modifier.width(280.dp).padding(vertical = 8.dp)) {
                DesktopDropdownMenuItem(text = "Checking", onClick = {})
                DesktopDropdownMenuItem(text = "Savings", onClick = {})
                DesktopDropdownMenuItem(text = "Credit", onClick = {})
                DesktopDropdownMenuItem(
                    text = "A financial institution with a name too long to fit on one line of the menu",
                    onClick = {},
                )
            }
        }
    }
}
