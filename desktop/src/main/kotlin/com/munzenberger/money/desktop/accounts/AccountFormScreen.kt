package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.desktop.components.DetailScreenHeader
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_load_error_message
import money.shared.generated.resources.edit_account_title
import money.shared.generated.resources.new_account_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Adds a new account, or edits the account with [accountId] if there is one. */
@Composable
fun AccountFormScreen(
    accountId: AccountId? = null,
    viewModel: AccountFormViewModel = koinViewModel { parametersOf(accountId) },
) {
    val state by viewModel.state.collectAsState()

    AccountFormScreenContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onAccountTypeChange = viewModel::onAccountTypeChange,
        onBankNameChange = viewModel::onBankNameChange,
        onBankChange = viewModel::onBankChange,
        onNumberChange = viewModel::onNumberChange,
        onInitialBalanceChange = viewModel::onInitialBalanceChange,
        onInitialBalanceFocusLost = viewModel::onInitialBalanceFocusLost,
        onMemoChange = viewModel::onMemoChange,
        onBackClick = viewModel::onBackClick,
        onSaveClick = viewModel::onSaveClick,
    )
}

@Composable
private fun AccountFormScreenContent(
    state: AccountFormUiState,
    onNameChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onBankNameChange: (String) -> Unit,
    onBankChange: (Bank) -> Unit,
    onNumberChange: (String) -> Unit,
    onInitialBalanceChange: (String) -> Unit,
    onInitialBalanceFocusLost: () -> Unit,
    onMemoChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    // The form is locked while saving, so what's saved is what's on screen.
    val enabled = state.saveState != SaveState.Saving

    Column(modifier = Modifier.fillMaxSize()) {
        DetailScreenHeader(
            title = stringResource(
                if (state.isEditing) Res.string.edit_account_title else Res.string.new_account_title
            ),
            onBackClick = onBackClick,
            backEnabled = enabled,
        )

        when (state.existingAccount) {
            is LoadState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LoadState.Error -> {
                Text(
                    text = stringResource(Res.string.account_load_error_message),
                    modifier = Modifier.padding(horizontal = MoneyTheme.spacing.medium),
                )
            }
            is LoadState.Loaded, null -> {
                // Only the fields scroll, so the header and its back button stay visible.
                AccountFormBody(
                    state = state,
                    enabled = enabled,
                    onNameChange = onNameChange,
                    onAccountTypeChange = onAccountTypeChange,
                    onBankNameChange = onBankNameChange,
                    onBankChange = onBankChange,
                    onNumberChange = onNumberChange,
                    onInitialBalanceChange = onInitialBalanceChange,
                    onInitialBalanceFocusLost = onInitialBalanceFocusLost,
                    onMemoChange = onMemoChange,
                    onCancelClick = onBackClick,
                    onSaveClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun AccountFormScreenPreview() {
    PreviewThemed {
        AccountFormScreenPreviewContent(
            state = AccountFormUiState(
                currencySymbol = "$",
                accountTypes = LoadState.Loaded(emptyList()),
                banks = LoadState.Loaded(emptyList()),
            )
        )
    }
}

@Preview
@Composable
private fun AccountFormScreenLoadingPreview() {
    PreviewThemed {
        AccountFormScreenPreviewContent(state = AccountFormUiState(currencySymbol = "$"))
    }
}

@Preview
@Composable
private fun AccountFormScreenErrorPreview() {
    PreviewThemed {
        AccountFormScreenPreviewContent(
            state = AccountFormUiState(
                accountTypes = LoadState.Error,
                banks = LoadState.Error,
                currencySymbol = "$",
            )
        )
    }
}

@Preview
@Composable
private fun AccountFormScreenValidationErrorPreview() {
    PreviewThemed {
        AccountFormScreenPreviewContent(
            state = AccountFormUiState(
                currencySymbol = "$",
                accountTypes = LoadState.Loaded(emptyList()),
                banks = LoadState.Loaded(emptyList()),
                initialBalance = "12abc",
                isNameError = true,
                isAccountTypeError = true,
                isInitialBalanceError = true,
            )
        )
    }
}

@Preview
@Composable
private fun AccountFormScreenSavingPreview() {
    PreviewThemed {
        AccountFormScreenPreviewContent(
            state = AccountFormUiState(
                name = "Checking",
                currencySymbol = "$",
                accountTypes = LoadState.Loaded(emptyList()),
                banks = LoadState.Loaded(emptyList()),
                saveState = SaveState.Saving,
            )
        )
    }
}

@Preview
@Composable
private fun AccountFormScreenSaveFailedPreview() {
    PreviewThemed {
        AccountFormScreenPreviewContent(
            state = AccountFormUiState(
                name = "Checking",
                currencySymbol = "$",
                accountTypes = LoadState.Loaded(emptyList()),
                banks = LoadState.Loaded(emptyList()),
                saveState = SaveState.Failed,
            )
        )
    }
}

@Preview
@Composable
private fun AccountFormScreenEditPreview() {
    val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
        value = AccountTypeConstant.Checking,
    )
    PreviewThemed {
        AccountFormScreenPreviewContent(
            state = AccountFormUiState(
                existingAccount = LoadState.Loaded(Account(name = "Checking", accountType = checking)),
                name = "Checking",
                accountType = checking,
                number = "1234-5678",
                initialBalance = "1,250.00",
                currencySymbol = "$",
                accountTypes = LoadState.Loaded(listOf(checking)),
                banks = LoadState.Loaded(emptyList()),
            )
        )
    }
}

@Preview
@Composable
private fun AccountFormScreenEditLoadErrorPreview() {
    PreviewThemed {
        AccountFormScreenPreviewContent(
            state = AccountFormUiState(existingAccount = LoadState.Error, currencySymbol = "$")
        )
    }
}

@Composable
private fun AccountFormScreenPreviewContent(state: AccountFormUiState) {
    AccountFormScreenContent(
        state = state,
        onNameChange = {},
        onAccountTypeChange = {},
        onBankNameChange = {},
        onBankChange = {},
        onNumberChange = {},
        onInitialBalanceChange = {},
        onInitialBalanceFocusLost = {},
        onMemoChange = {},
        onBackClick = {},
        onSaveClick = {},
    )
}
