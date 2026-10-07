package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.bank.Bank
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_name_error_message
import money.shared.generated.resources.account_name_label
import money.shared.generated.resources.account_number_label
import money.shared.generated.resources.account_type_asset
import money.shared.generated.resources.account_type_error_message
import money.shared.generated.resources.account_type_cash
import money.shared.generated.resources.account_type_checking
import money.shared.generated.resources.account_type_credit
import money.shared.generated.resources.account_type_label
import money.shared.generated.resources.account_type_load_error_message
import money.shared.generated.resources.account_type_loan
import money.shared.generated.resources.account_type_savings
import money.shared.generated.resources.comments_label
import money.shared.generated.resources.financial_institution_label
import money.shared.generated.resources.financial_institution_load_error_message
import money.shared.generated.resources.initial_balance_error_message
import money.shared.generated.resources.initial_balance_label
import money.shared.generated.resources.required_field_label
import org.jetbrains.compose.resources.stringResource

/** Required field for the account's name. */
@Composable
internal fun NameField(
    name: String,
    isError: Boolean,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        enabled = enabled,
        label = {
            Text(
                stringResource(
                    Res.string.required_field_label,
                    stringResource(Res.string.account_name_label),
                )
            )
        },
        supportingText = if (isError) {
            { Text(stringResource(Res.string.account_name_error_message)) }
        } else {
            null
        },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Required dropdown for picking the account type. It starts out empty, but there's no empty option in
 * the menu, so once a type is picked it can only be changed to another type. The field is disabled until
 * the account types load, and shows an error if they can't be loaded, since the form can't be completed
 * without one, or if the user tries to save without picking one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountTypeField(
    accountTypes: LoadState<List<AccountType>>,
    accountType: AccountType?,
    isMissing: Boolean,
    enabled: Boolean,
    onAccountTypeChange: (AccountType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val isLoadError = accountTypes is LoadState.Error
    val isError = isLoadError || isMissing
    val isEnabled = enabled && accountTypes is LoadState.Loaded

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled,
        onExpandedChange = { expanded = it && isEnabled },
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
            supportingText = when {
                isLoadError -> {
                    { Text(stringResource(Res.string.account_type_load_error_message)) }
                }
                isMissing -> {
                    { Text(stringResource(Res.string.account_type_error_message)) }
                }
                else -> null
            },
            isError = isError,
            enabled = isEnabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && isEnabled) },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = isEnabled),
        )

        ExposedDropdownMenu(
            expanded = expanded && isEnabled,
            onDismissRequest = { expanded = false },
        ) {
            accountTypes.loadedOrEmpty.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.value.label(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
internal fun FinancialInstitutionField(
    banks: LoadState<List<Bank>>,
    bankName: String,
    bank: Bank?,
    enabled: Boolean,
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
        expanded = expanded && enabled && options.isNotEmpty(),
        onExpandedChange = { expanded = it && enabled },
    ) {
        OutlinedTextField(
            value = bankName,
            enabled = enabled,
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
                    expanded = expanded && enabled && options.isNotEmpty(),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable, enabled = enabled),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = enabled)
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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

/**
 * Optional field for the account number. It takes any characters, since account numbers can include
 * letters and dashes.
 */
@Composable
internal fun AccountNumberField(
    number: String,
    enabled: Boolean,
    onNumberChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = number,
        onValueChange = onNumberChange,
        enabled = enabled,
        label = { Text(stringResource(Res.string.account_number_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Optional field for the account's opening balance; blank means zero. The amount is checked when
 * the user leaves the field, so a half-typed amount like "-" isn't flagged while they're still typing.
 */
@Composable
internal fun InitialBalanceField(
    initialBalance: String,
    currencySymbol: String,
    isError: Boolean,
    enabled: Boolean,
    onInitialBalanceChange: (String) -> Unit,
    onFocusLost: () -> Unit,
) {
    // onFocusChanged also reports the initial unfocused state, which isn't the user leaving the field.
    var hasFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = initialBalance,
        onValueChange = onInitialBalanceChange,
        enabled = enabled,
        label = { Text(stringResource(Res.string.initial_balance_label)) },
        placeholder = { Text(Money(0).toString(asCurrency = false)) },
        prefix = { Text(currencySymbol) },
        supportingText = if (isError) {
            { Text(stringResource(Res.string.initial_balance_error_message)) }
        } else {
            null
        },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (hasFocus && !it.isFocused) onFocusLost()
                hasFocus = it.isFocused
            },
    )
}

/**
 * Optional multiline field for comments about the account, stored as its memo. It shows three lines to
 * start, and scrolls within itself past six.
 */
@Composable
internal fun CommentsField(
    memo: String,
    enabled: Boolean,
    onMemoChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = memo,
        onValueChange = onMemoChange,
        enabled = enabled,
        label = { Text(stringResource(Res.string.comments_label)) },
        minLines = 3,
        maxLines = 6,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth(),
    )
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
