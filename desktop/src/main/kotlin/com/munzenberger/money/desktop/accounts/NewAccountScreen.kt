package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.desktop.components.DetailScreenHeader
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.new_account_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewAccountScreen(viewModel: NewAccountViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    NewAccountScreenContent(
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
    )
}

@Composable
private fun NewAccountScreenContent(
    state: NewAccountUiState,
    onNameChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onBankNameChange: (String) -> Unit,
    onBankChange: (Bank) -> Unit,
    onNumberChange: (String) -> Unit,
    onInitialBalanceChange: (String) -> Unit,
    onInitialBalanceFocusLost: () -> Unit,
    onMemoChange: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DetailScreenHeader(
            title = stringResource(Res.string.new_account_title),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier.padding(horizontal = MoneyTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.small),
        ) {
            NameField(name = state.name, onNameChange = onNameChange)

            AccountTypeField(
                accountTypes = state.accountTypes,
                accountType = state.accountType,
                onAccountTypeChange = onAccountTypeChange,
            )

            FinancialInstitutionField(
                banks = state.banks,
                bankName = state.bankName,
                bank = state.bank,
                onBankNameChange = onBankNameChange,
                onBankChange = onBankChange,
            )

            AccountNumberField(number = state.number, onNumberChange = onNumberChange)

            InitialBalanceField(
                initialBalance = state.initialBalance,
                currencySymbol = state.currencySymbol,
                isError = state.isInitialBalanceError,
                onInitialBalanceChange = onInitialBalanceChange,
                onFocusLost = onInitialBalanceFocusLost,
            )

            CommentsField(memo = state.memo, onMemoChange = onMemoChange)
        }
    }
}

@Preview
@Composable
private fun NewAccountScreenPreview() {
    PreviewThemed {
        NewAccountScreenPreviewContent(
            state = NewAccountUiState(
                currencySymbol = "$",
                accountTypes = LoadState.Loaded(emptyList()),
                banks = LoadState.Loaded(emptyList()),
            )
        )
    }
}

@Preview
@Composable
private fun NewAccountScreenLoadingPreview() {
    PreviewThemed {
        NewAccountScreenPreviewContent(state = NewAccountUiState(currencySymbol = "$"))
    }
}

@Preview
@Composable
private fun NewAccountScreenErrorPreview() {
    PreviewThemed {
        NewAccountScreenPreviewContent(
            state = NewAccountUiState(
                accountTypes = LoadState.Error,
                banks = LoadState.Error,
                currencySymbol = "$",
            )
        )
    }
}

@Composable
private fun NewAccountScreenPreviewContent(state: NewAccountUiState) {
    NewAccountScreenContent(
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
    )
}
