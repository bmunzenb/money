package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.desktop.components.FormDefaults
import com.munzenberger.money.desktop.components.FormSectionCard
import com.munzenberger.money.desktop.components.ScrollableColumn
import com.munzenberger.money.shared.theme.MoneyTheme
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_section_title
import money.shared.generated.resources.additional_information_section_title
import money.shared.generated.resources.balance_and_reference_section_title
import money.shared.generated.resources.edit_account_description
import money.shared.generated.resources.new_account_description
import money.shared.generated.resources.required_field_legend
import org.jetbrains.compose.resources.stringResource

/** The account form's fields, grouped into sections, followed by its buttons. */
@Composable
internal fun AccountFormBody(
    state: AccountFormUiState,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onBankNameChange: (String) -> Unit,
    onBankChange: (Bank) -> Unit,
    onNumberChange: (String) -> Unit,
    onInitialBalanceChange: (String) -> Unit,
    onInitialBalanceFocusLost: () -> Unit,
    onMemoChange: (String) -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScrollableColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = MoneyTheme.spacing.medium,
            end = MoneyTheme.spacing.medium,
            bottom = MoneyTheme.spacing.medium,
        ),
    ) {
        // The scroll area stays full width, so its scrollbar sits at the window edge, but the form
        // itself is capped so its fields don't stretch across wide windows.
        Column(
            modifier = Modifier.widthIn(max = FormDefaults.MaxWidth),
            verticalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.medium),
        ) {
            AccountFormIntro(isEditing = state.isEditing)

            AccountSection(
                state = state,
                enabled = enabled,
                onNameChange = onNameChange,
                onAccountTypeChange = onAccountTypeChange,
                onBankNameChange = onBankNameChange,
                onBankChange = onBankChange,
            )

            FormSectionCard(title = stringResource(Res.string.balance_and_reference_section_title)) {
                AccountNumberField(number = state.number, enabled = enabled, onNumberChange = onNumberChange)

                InitialBalanceField(
                    initialBalance = state.initialBalance,
                    currencySymbol = state.currencySymbol,
                    isError = state.isInitialBalanceError,
                    enabled = enabled,
                    onInitialBalanceChange = onInitialBalanceChange,
                    onFocusLost = onInitialBalanceFocusLost,
                )
            }

            FormSectionCard(title = stringResource(Res.string.additional_information_section_title)) {
                CommentsField(memo = state.memo, enabled = enabled, onMemoChange = onMemoChange)
            }

            AccountFormButtons(
                saveState = state.saveState,
                onCancelClick = onCancelClick,
                onSaveClick = onSaveClick,
            )
        }
    }
}

/** The account's name, type, and financial institution. */
@Composable
private fun AccountSection(
    state: AccountFormUiState,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onBankNameChange: (String) -> Unit,
    onBankChange: (Bank) -> Unit,
) {
    FormSectionCard(title = stringResource(Res.string.account_section_title)) {
        NameField(
            name = state.name,
            isError = state.isNameError,
            enabled = enabled,
            onNameChange = onNameChange,
        )

        AccountTypeField(
            accountTypes = state.accountTypes,
            accountType = state.accountType,
            isMissing = state.isAccountTypeError,
            enabled = enabled,
            onAccountTypeChange = onAccountTypeChange,
        )

        FinancialInstitutionField(
            banks = state.banks,
            bankName = state.bankName,
            bank = state.bank,
            enabled = enabled,
            onBankNameChange = onBankNameChange,
            onBankChange = onBankChange,
        )
    }
}

/** What the form is for, and what the asterisk on required field labels means. */
@Composable
private fun AccountFormIntro(isEditing: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.small)) {
        Text(
            text = stringResource(
                if (isEditing) Res.string.edit_account_description else Res.string.new_account_description
            ),
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = stringResource(Res.string.required_field_legend),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
