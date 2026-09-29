package com.munzenberger.money.desktop.toolbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.accounts_button_title
import money.shared.generated.resources.categories_button_title
import money.shared.generated.resources.navigate_back_button_description
import money.shared.generated.resources.payees_button_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppToolBar(viewModel: AppToolBarViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    AppToolBarContent(
        isBackEnabled = state.isBackEnabled,
        onBackClick = viewModel::onBackClick,
        onAccountsClick = {},
        onCategoriesClick = {},
        onPayeesClick = {},
    )
}

@Composable
private fun AppToolBarContent(
    isBackEnabled: Boolean,
    onBackClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onPayeesClick: () -> Unit,
) {
    Row {
        IconButton(onClick = onBackClick, enabled = isBackEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.navigate_back_button_description),
            )
        }
        TextButton(onClick = onAccountsClick) {
            Icon(
                imageVector = Icons.Filled.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.accounts_button_title))
        }
        TextButton(onClick = onCategoriesClick) {
            Icon(
                imageVector = Icons.Filled.Category,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.categories_button_title))
        }
        TextButton(onClick = onPayeesClick) {
            Icon(
                imageVector = Icons.Filled.People,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.payees_button_title))
        }
    }
}

@Preview
@Composable
private fun AppToolBarBackEnabledPreview() {
    PreviewThemed {
        AppToolBarContent(
            isBackEnabled = true,
            onBackClick = {},
            onAccountsClick = {},
            onCategoriesClick = {},
            onPayeesClick = {},
        )
    }
}

@Preview
@Composable
private fun AppToolBarBackDisabledPreview() {
    PreviewThemed {
        AppToolBarContent(
            isBackEnabled = false,
            onBackClick = {},
            onAccountsClick = {},
            onCategoriesClick = {},
            onPayeesClick = {},
        )
    }
}
