package com.munzenberger.money.desktop.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.back_button_description
import org.jetbrains.compose.resources.stringResource

/**
 * Header for screens pushed on top of another screen: a back button followed by the screen title. The
 * back arrow lines up with [ListScreenHeader]'s title, so the content edge stays put when navigating
 * between the two.
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
            .padding(
                start = MoneyTheme.spacing.medium - IconButtonIconInset,
                end = MoneyTheme.spacing.medium,
                top = MoneyTheme.spacing.small,
                bottom = MoneyTheme.spacing.small,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, enabled = backEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.back_button_description),
            )
        }
        Text(
            text = title,
            style = MoneyTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
    }
}

// IconButton centers its 24dp icon in a 48dp touch target, so its icon starts 12dp in. Subtracting this
// from the row's start padding lines the back arrow up with ListScreenHeader's title.
private val IconButtonIconInset = 12.dp

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
