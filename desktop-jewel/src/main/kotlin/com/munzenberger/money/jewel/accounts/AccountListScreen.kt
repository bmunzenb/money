package com.munzenberger.money.jewel.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.jewel.theme.ContentBackground
import com.munzenberger.money.jewel.theme.PreviewThemed
import com.munzenberger.money.presentation.accounts.AccountListUiState
import com.munzenberger.money.presentation.accounts.AccountListViewModel
import com.munzenberger.money.presentation.accounts.LoadState
import com.munzenberger.money.presentation.accounts.label
import money.presentation.generated.resources.Res
import money.presentation.generated.resources.account_grouping_label
import money.presentation.generated.resources.account_list_empty_message
import money.presentation.generated.resources.account_list_error_message
import money.presentation.generated.resources.account_list_title
import money.presentation.generated.resources.add_account_button_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.typography
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ContentBackground)
    ) {
        AccountListHeader(
            grouping = state.grouping,
            onGroupingChange = onGroupingChange,
            onAddAccountClick = onAddAccountClick,
        )

        Divider(orientation = Orientation.Horizontal, modifier = Modifier.fillMaxWidth())

        Box(modifier = Modifier.weight(1f)) {
            AccountListBody(groups = state.groups, onAccountClick = onAccountClick)
        }
    }
}

/** The title, with the grouping and the new account action at the end of the same row, fixed above the list. */
@Composable
private fun AccountListHeader(
    grouping: AccountGrouping,
    onGroupingChange: (AccountGrouping) -> Unit,
    onAddAccountClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.account_list_title),
            style = JewelTheme.typography.h1TextStyle,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )

        AccountGroupingField(grouping = grouping, onGroupingChange = onGroupingChange)

        DefaultButton(onClick = onAddAccountClick) {
            Text(text = stringResource(Res.string.add_account_button_title))
        }
    }
}

/** A "Group by" combo box for picking how the accounts are grouped. */
@Composable
private fun AccountGroupingField(
    grouping: AccountGrouping,
    onGroupingChange: (AccountGrouping) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = stringResource(Res.string.account_grouping_label))

        val options = AccountGrouping.entries
        ListComboBox(
            items = options.map { it.label() },
            selectedIndex = options.indexOf(grouping),
            onSelectedItemChange = { onGroupingChange(options[it]) },
            modifier = Modifier.width(GroupingFieldWidth),
        )
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
            AccountListMessage(
                text = stringResource(Res.string.account_list_error_message),
                isError = true,
            )
        }
        is LoadState.Loaded -> {
            if (groups.value.isEmpty()) {
                AccountListMessage(text = stringResource(Res.string.account_list_empty_message))
            } else {
                AccountGroupList(groups = groups.value, onAccountClick = onAccountClick)
            }
        }
    }
}

@Composable
private fun AccountGroupList(
    groups: List<AccountGroup>,
    onAccountClick: (Account) -> Unit,
) {
    val listState = rememberLazyListState()

    VerticallyScrollableContainer(scrollState = listState, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 16.dp),
        ) {
            items(groups, key = { it.key() }) { group ->
                var expanded by rememberSaveable { mutableStateOf(true) }
                AccountGroupSection(
                    group = group,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onAccountClick = onAccountClick,
                )
            }
        }
    }
}

@Composable
private fun AccountListMessage(text: String, isError: Boolean = false) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = if (isError) JewelTheme.globalColors.text.error else JewelTheme.globalColors.text.info,
        )
    }
}

private val GroupingFieldWidth = 200.dp


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
                                Account(
                                    name = "Everyday checking",
                                    number = "123454821",
                                    accountType = previewAccountType,
                                ),
                                Account(
                                    name = "Joint checking",
                                    number = "987650937",
                                    accountType = previewAccountType,
                                ),
                                Account(name = "Old checking", accountType = previewAccountType, isClosed = true),
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
