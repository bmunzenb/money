package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.desktop.components.ListScreenActionButton
import com.munzenberger.money.desktop.components.ListScreenHeader
import com.munzenberger.money.desktop.components.ScrollableLazyColumn
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_grouping_label
import money.shared.generated.resources.account_list_empty_message
import money.shared.generated.resources.account_list_error_message
import money.shared.generated.resources.account_list_title
import money.shared.generated.resources.add_account_button_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountListScreen(viewModel: AccountListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState(initial = AccountListUiState())

    AccountListScreenContent(
        state = state,
        onGroupingChange = viewModel::onGroupingChange,
        onAddAccountClick = viewModel::onAddAccountClick,
        onAccountClick = viewModel::onAccountClick,
    )
}

@Composable
private fun AccountListScreenContent(
    state: AccountListUiState,
    onGroupingChange: (AccountGrouping) -> Unit,
    onAddAccountClick: () -> Unit,
    onAccountClick: (Account) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListScreenHeader(title = stringResource(Res.string.account_list_title))

        // The new account action sits with the grouping, in a row of controls above the list.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MoneyTheme.spacing.medium,
                    end = MoneyTheme.spacing.medium,
                    bottom = MoneyTheme.spacing.medium,
                ),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.medium),
        ) {
            AccountGroupingField(
                grouping = state.grouping,
                onGroupingChange = onGroupingChange,
            )
            Spacer(modifier = Modifier.weight(1f))
            ListScreenActionButton(
                label = stringResource(Res.string.add_account_button_title),
                onClick = onAddAccountClick,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            AccountListBody(groups = state.groups, onAccountClick = onAccountClick)
        }
    }
}

/** Dropdown for picking how the accounts are grouped. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountGroupingField(
    grouping: AccountGrouping,
    onGroupingChange: (AccountGrouping) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = grouping.label(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.account_grouping_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AccountGrouping.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.label(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        onGroupingChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun AccountListBody(
    groups: LoadState<List<AccountGroup>>,
    onAccountClick: (Account) -> Unit,
) {
    when (groups) {
        is LoadState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> {
            Text(text = stringResource(Res.string.account_list_error_message))
        }
        is LoadState.Loaded -> {
            if (groups.value.isEmpty()) {
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
                    items(groups.value, key = { it.key() }) { group ->
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
            state = AccountListUiState(groups = LoadState.Loading),
            onGroupingChange = {},
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
            state = AccountListUiState(groups = LoadState.Error),
            onGroupingChange = {},
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
            state = AccountListUiState(
                grouping = AccountGrouping.AccountType,
                groups = LoadState.Loaded(
                    listOf(
                        AccountGroup.ByAccountType(
                            accountType = previewAccountType,
                            accounts = listOf(
                                Account(name = "Checking", accountType = previewAccountType),
                                Account(name = "Savings", accountType = previewAccountType),
                            ),
                        ),
                    ),
                ),
            ),
            onGroupingChange = {},
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
            state = AccountListUiState(groups = LoadState.Loaded(emptyList())),
            onGroupingChange = {},
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
