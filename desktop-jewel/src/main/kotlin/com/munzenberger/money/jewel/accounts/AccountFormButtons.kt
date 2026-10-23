package com.munzenberger.money.jewel.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.munzenberger.money.presentation.accounts.SaveState
import money.presentation.generated.resources.Res
import money.presentation.generated.resources.cancel_button_title
import money.presentation.generated.resources.save_account_error_message
import money.presentation.generated.resources.save_button_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

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
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (saveState == SaveState.Failed) {
            Text(
                text = stringResource(Res.string.save_account_error_message),
                color = JewelTheme.globalColors.text.error,
                modifier = Modifier
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        OutlinedButton(onClick = onCancelClick, enabled = !isSaving) {
            Text(text = stringResource(Res.string.cancel_button_title))
        }

        DefaultButton(onClick = onSaveClick, enabled = !isSaving) {
            // The label stays in place, hidden, so the button keeps its width and accessible name.
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.save_button_title),
                    modifier = Modifier.alpha(if (isSaving) 0f else 1f),
                )
                if (isSaving) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
