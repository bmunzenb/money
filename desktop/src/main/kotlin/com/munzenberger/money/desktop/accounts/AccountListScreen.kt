package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.desktop.components.ListScreenHeader
import com.munzenberger.money.desktop.components.ScrollableLazyColumn
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_list_empty_message
import money.shared.generated.resources.account_list_error_message
import money.shared.generated.resources.account_list_title
import money.shared.generated.resources.add_account_button_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountListScreen(viewModel: AccountListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState(initial = AccountListUiState.Loading)

    AccountListScreenContent(
        state = state,
        onAddAccountClick = viewModel::onAddAccountClick,
        // There's no account screen to open yet.
        onAccountClick = {},
    )
}

@Composable
private fun AccountListScreenContent(
    state: AccountListUiState,
    onAddAccountClick: () -> Unit,
    onAccountClick: (Account) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListScreenHeader(
            title = stringResource(Res.string.account_list_title),
            actionLabel = stringResource(Res.string.add_account_button_title),
            onActionClick = onAddAccountClick,
        )

        Box(modifier = Modifier.weight(1f)) {
            AccountListBody(state = state, onAccountClick = onAccountClick)
        }
    }
}

@Composable
private fun AccountListBody(
    state: AccountListUiState,
    onAccountClick: (Account) -> Unit,
) {
    when (state) {
        is AccountListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AccountListUiState.Error -> {
            Text(text = stringResource(Res.string.account_list_error_message))
        }
        is AccountListUiState.Content -> {
            if (state.groups.isEmpty()) {
                Text(text = stringResource(Res.string.account_list_empty_message))
            } else {
                ScrollableLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = MoneyTheme.spacing.medium,
                        end = MoneyTheme.spacing.medium,
                        bottom = MoneyTheme.spacing.medium,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.medium),
                ) {
                    items(state.groups, key = { it.key() }) { group ->
                        var expanded by rememberSaveable { mutableStateOf(true) }
                        AccountGroupCard(
                            group = group,
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            onAccountClick = onAccountClick,
                        )
                    }
                }
            }
        }
    }
}

/** A key that identifies the group across emissions, so its card keeps its expanded state. */
private fun AccountGroup.key(): String = when (this) {
    is AccountGroup.All -> "all"
    is AccountGroup.ByAccountType -> "type:${accountType.id.value}"
    is AccountGroup.ByAccountClass -> "class:${accountClass.id.value}"
    is AccountGroup.ByBank -> "bank:${bank?.id?.value}"
}

@Preview
@Composable
private fun AccountListScreenLoadingPreview() {
    PreviewThemed {
        AccountListScreenContent(
            state = AccountListUiState.Loading,
            onAddAccountClick = {},
            onAccountClick = {},
        )
    }
}

@Preview
@Composable
private fun AccountListScreenErrorPreview() {
    PreviewThemed {
        AccountListScreenContent(
            state = AccountListUiState.Error,
            onAddAccountClick = {},
            onAccountClick = {},
        )
    }
}

@Preview
@Composable
private fun AccountListScreenWithAccountsPreview() {
    PreviewThemed {
        AccountListScreenContent(
            state = AccountListUiState.Content(
                groups = listOf(
                    AccountGroup.All(
                        accounts = listOf(
                            Account(name = "Checking", accountType = previewAccountType),
                            Account(name = "Savings", accountType = previewAccountType),
                        ),
                    ),
                ),
            ),
            onAddAccountClick = {},
            onAccountClick = {},
        )
    }
}

@Preview
@Composable
private fun AccountListScreenEmptyPreview() {
    PreviewThemed {
        AccountListScreenContent(
            state = AccountListUiState.Content(groups = emptyList()),
            onAddAccountClick = {},
            onAccountClick = {},
        )
    }
}

private val previewAccountType = AccountType(
    id = AccountTypeId(1),
    accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
    value = AccountTypeConstant.Checking,
)
