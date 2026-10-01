package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeGroup
import com.munzenberger.money.data.api.account.AccountTypeGroupConstant
import com.munzenberger.money.data.api.account.AccountTypeGroupId
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_list_empty_message
import money.shared.generated.resources.account_list_error_message
import money.shared.generated.resources.account_list_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountListScreen(viewModel: AccountListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState(initial = AccountListUiState.Loading)

    AccountListScreenContent(state = state)
}

@Composable
private fun AccountListScreenContent(state: AccountListUiState) {
    Column {
        Text(
            text = stringResource(Res.string.account_list_title),
            style = MoneyTheme.typography.headlineMedium
        )

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
                if (state.accounts.isEmpty()) {
                    Text(text = stringResource(Res.string.account_list_empty_message))
                } else {
                    LazyColumn {
                        items(state.accounts, key = { it.id.value }) { account ->
                            Text(text = account.name)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AccountListScreenLoadingPreview() {
    PreviewThemed {
        AccountListScreenContent(state = AccountListUiState.Loading)
    }
}

@Preview
@Composable
private fun AccountListScreenErrorPreview() {
    PreviewThemed {
        AccountListScreenContent(state = AccountListUiState.Error)
    }
}

@Preview
@Composable
private fun AccountListScreenWithAccountsPreview() {
    PreviewThemed {
        AccountListScreenContent(
            state = AccountListUiState.Content(
                accounts = listOf(
                    Account(name = "Checking", accountType = previewAccountType),
                    Account(name = "Savings", accountType = previewAccountType),
                )
            )
        )
    }
}

@Preview
@Composable
private fun AccountListScreenEmptyPreview() {
    PreviewThemed {
        AccountListScreenContent(state = AccountListUiState.Content(accounts = emptyList()))
    }
}

private val previewAccountType = AccountType(
    id = AccountTypeId(1),
    group = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets),
    value = AccountTypeConstant.Checking,
)
