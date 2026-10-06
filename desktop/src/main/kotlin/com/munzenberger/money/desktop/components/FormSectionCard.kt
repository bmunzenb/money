package com.munzenberger.money.desktop.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed

/** An outlined card with a title, grouping related fields of a form. */
@Composable
fun FormSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MoneyTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Column(
            modifier = Modifier.padding(MoneyTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.small),
        ) {
            Text(
                text = title,
                style = MoneyTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )

            content()
        }
    }
}

@Preview
@Composable
private fun FormSectionCardPreview() {
    PreviewThemed {
        FormSectionCard(title = "Account") {
            OutlinedTextField(
                value = "Checking",
                onValueChange = {},
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
