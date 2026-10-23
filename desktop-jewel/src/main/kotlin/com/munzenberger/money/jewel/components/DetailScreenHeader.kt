package com.munzenberger.money.jewel.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.munzenberger.money.jewel.theme.PreviewThemed
import money.presentation.generated.resources.Res
import money.presentation.generated.resources.back_button_description
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography

/**
 * Header for screens pushed on top of another screen: a back button followed by the screen title, in the
 * same place and style as the account list's title.
 */
@Composable
fun DetailScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    backEnabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp - IconButtonIconInset, top = 20.dp, end = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconActionButton(
            key = AllIconsKeys.Actions.Back,
            contentDescription = stringResource(Res.string.back_button_description),
            onClick = onBackClick,
            enabled = backEnabled,
        )
        Text(
            text = title,
            style = JewelTheme.typography.h1TextStyle,
            modifier = Modifier.semantics { heading() },
        )
    }
}

// The icon button pads its 16dp icon, so its icon starts a few dp in. Subtracting this from the row's start
// padding lines the back arrow up with the account list's title.
private val IconButtonIconInset = 4.dp

@Preview
@Composable
private fun DetailScreenHeaderPreview() {
    PreviewThemed {
        DetailScreenHeader(
            title = "New account",
            onBackClick = {},
        )
    }
}
