package com.munzenberger.money.desktop.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed

/**
 * Header for list screens: the screen title, with the screen's primary action at the end. It's placed
 * above the list rather than inside it, so the action stays visible while the list scrolls.
 */
@Composable
fun ListScreenHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector = Icons.Filled.Add,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MoneyTheme.spacing.medium, vertical = MoneyTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MoneyTheme.typography.headlineMedium,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        Button(onClick = onActionClick) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = actionLabel)
        }
    }
}

@Preview
@Composable
private fun ListScreenHeaderPreview() {
    PreviewThemed {
        ListScreenHeader(
            title = "Accounts",
            actionLabel = "New account",
            onActionClick = {},
        )
    }
}
