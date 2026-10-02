package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.desktop.components.DetailScreenHeader
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_name_label
import money.shared.generated.resources.account_number_label
import money.shared.generated.resources.account_type_asset
import money.shared.generated.resources.account_type_cash
import money.shared.generated.resources.account_type_checking
import money.shared.generated.resources.account_type_credit
import money.shared.generated.resources.account_type_label
import money.shared.generated.resources.account_type_load_error_message
import money.shared.generated.resources.account_type_loan
import money.shared.generated.resources.account_type_savings
import money.shared.generated.resources.financial_institution_label
import money.shared.generated.resources.financial_institution_load_error_message
import money.shared.generated.resources.new_account_title
import money.shared.generated.resources.required_field_label
import money.shared.generated.resources.required_field_supporting_text
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
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = {
                    Text(
                        stringResource(
                            Res.string.required_field_label,
                            stringResource(Res.string.account_name_label),
                        )
                    )
                },
                supportingText = { Text(stringResource(Res.string.required_field_supporting_text)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

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

            OutlinedTextField(
                value = state.number,
                onValueChange = onNumberChange,
                label = { Text(stringResource(Res.string.account_number_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Required dropdown for picking the account type. It starts out empty, but there's no empty option in
 * the menu, so once a type is picked it can only be changed to another type. The field is disabled until
 * the account types load, and shows an error if they can't be loaded, since the form can't be completed
 * without one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountTypeField(
    accountTypes: LoadState<List<AccountType>>,
    accountType: AccountType?,
    onAccountTypeChange: (AccountType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val enabled = accountTypes is LoadState.Loaded
    val isError = accountTypes is LoadState.Error

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { expanded = it && enabled },
    ) {
        OutlinedTextField(
            value = accountType?.value?.label().orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    stringResource(
                        Res.string.required_field_label,
                        stringResource(Res.string.account_type_label),
                    )
                )
            },
            supportingText = {
                Text(
                    stringResource(
                        if (isError) {
                            Res.string.account_type_load_error_message
                        } else {
                            Res.string.required_field_supporting_text
                        }
                    )
                )
            },
            isError = isError,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            accountTypes.loadedOrEmpty.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.value.label()) },
                    onClick = {
                        onAccountTypeChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/**
 * Optional editable dropdown for the account's financial institution. The user can pick an existing
 * bank from the menu or type the name of a new one. As they type, the menu narrows to the banks whose
 * names contain the text; once the text names an existing bank, the menu lists every bank again so the
 * user can switch to another one. If the banks can't be loaded, the field still takes a typed name, and
 * says that existing banks aren't available.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinancialInstitutionField(
    banks: LoadState<List<Bank>>,
    bankName: String,
    bank: Bank?,
    onBankNameChange: (String) -> Unit,
    onBankChange: (Bank) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val allBanks = banks.loadedOrEmpty
    val options = if (bank != null) {
        allBanks
    } else {
        allBanks.filter { it.name.contains(bankName.trim(), ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && options.isNotEmpty(),
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = bankName,
            onValueChange = {
                onBankNameChange(it)
                expanded = true
            },
            label = { Text(stringResource(Res.string.financial_institution_label)) },
            supportingText = if (banks is LoadState.Error) {
                { Text(stringResource(Res.string.financial_institution_load_error_message)) }
            } else {
                null
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded && options.isNotEmpty(),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onBankChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun AccountTypeConstant.label(): String = stringResource(
    when (this) {
        AccountTypeConstant.Savings -> Res.string.account_type_savings
        AccountTypeConstant.Checking -> Res.string.account_type_checking
        AccountTypeConstant.Asset -> Res.string.account_type_asset
        AccountTypeConstant.Cash -> Res.string.account_type_cash
        AccountTypeConstant.Credit -> Res.string.account_type_credit
        AccountTypeConstant.Loan -> Res.string.account_type_loan
    }
)

@Preview
@Composable
private fun NewAccountScreenPreview() {
    PreviewThemed {
        NewAccountScreenPreviewContent(
            state = NewAccountUiState(
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
        NewAccountScreenPreviewContent(state = NewAccountUiState())
    }
}

@Preview
@Composable
private fun NewAccountScreenErrorPreview() {
    PreviewThemed {
        NewAccountScreenPreviewContent(
            state = NewAccountUiState(accountTypes = LoadState.Error, banks = LoadState.Error)
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
        onBackClick = {},
    )
}
