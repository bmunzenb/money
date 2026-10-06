package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.desktop.components.DetailScreenHeader
import com.munzenberger.money.desktop.components.FormSectionCard
import com.munzenberger.money.desktop.components.ScrollableColumn
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_section_title
import money.shared.generated.resources.additional_information_section_title
import money.shared.generated.resources.balance_and_reference_section_title
import money.shared.generated.resources.new_account_description
import money.shared.generated.resources.new_account_title
import money.shared.generated.resources.required_field_legend
import money.shared.generated.resources.save_button_title
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
        onSaveClick = viewModel::onSaveClick,
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
    onSaveClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DetailScreenHeader(
            title = stringResource(Res.string.new_account_title),
            onBackClick = onBackClick,
        ) {
            Button(onClick = onSaveClick) {
                Text(text = stringResource(Res.string.save_button_title))
            }
        }

        // Only the fields scroll, so the header and its back button stay visible.
        ScrollableColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = MoneyTheme.spacing.medium,
                end = MoneyTheme.spacing.medium,
                bottom = MoneyTheme.spacing.medium,
            ),
            verticalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.small),
        ) {
            Text(
                text = stringResource(Res.string.new_account_description),
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = stringResource(Res.string.required_field_legend),
                style = MaterialTheme.typography.bodySmall,
            )

            FormSectionCard(title = stringResource(Res.string.account_section_title)) {
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
            }

            FormSectionCard(title = stringResource(Res.string.balance_and_reference_section_title)) {
                AccountNumberField(number = state.number, onNumberChange = onNumberChange)

                InitialBalanceField(
                    initialBalance = state.initialBalance,
                    currencySymbol = state.currencySymbol,
                    isError = state.isInitialBalanceError,
                    onInitialBalanceChange = onInitialBalanceChange,
                    onFocusLost = onInitialBalanceFocusLost,
                )
            }

            FormSectionCard(title = stringResource(Res.string.additional_information_section_title)) {
                CommentsField(memo = state.memo, onMemoChange = onMemoChange)
            }
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
        onSaveClick = {},
    )
}
