package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.add_account_button_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AccountListSidebar(
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxHeight(),
        color = MoneyTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            TextButton(onClick = onAddAccountClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(Res.string.add_account_button_title))
            }
        }
    }
}

@Preview
@Composable
private fun AccountListSidebarPreview() {
    PreviewThemed {
        AccountListSidebar(onAddAccountClick = {})
    }
}
