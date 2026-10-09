package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.munzenberger.money.shared.theme.MoneyTheme
import money.shared.generated.resources.Res
import money.shared.generated.resources.cancel_button_title
import money.shared.generated.resources.save_account_error_message
import money.shared.generated.resources.save_button_title
import org.jetbrains.compose.resources.stringResource

/**
 * Cancel and Save. While saving, both are disabled and Save shows a spinner in place of its label; if
 * saving fails, the reason is shown beside them.
 */
@Composable
internal fun AccountFormButtons(
    saveState: SaveState,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val isSaving = saveState == SaveState.Saving

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.small, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (saveState == SaveState.Failed) {
            Text(
                text = stringResource(Res.string.save_account_error_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        TextButton(onClick = onCancelClick, enabled = !isSaving) {
            Text(text = stringResource(Res.string.cancel_button_title))
        }

        Button(onClick = onSaveClick, enabled = !isSaving) {
            // The label stays in place, hidden, so the button keeps its width and accessible name.
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.save_button_title),
                    modifier = Modifier.alpha(if (isSaving) 0f else 1f),
                )
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}
