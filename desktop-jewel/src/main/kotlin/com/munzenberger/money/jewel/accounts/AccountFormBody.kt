package com.munzenberger.money.jewel.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.jewel.components.FormSection
import com.munzenberger.money.presentation.accounts.AccountFormUiState
import money.presentation.generated.resources.Res
import money.presentation.generated.resources.account_section_title
import money.presentation.generated.resources.additional_information_section_title
import money.presentation.generated.resources.balance_and_reference_section_title
import money.presentation.generated.resources.edit_account_description
import money.presentation.generated.resources.new_account_description
import money.presentation.generated.resources.required_field_legend
import money.presentation.generated.resources.status_section_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer

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
    onIsClosedChange: (Boolean) -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The scroll area stays full width, so its scrollbar sits at the window edge, but the form itself is
    // capped so its fields don't stretch across wide windows. The container scrolls its content itself,
    // so the column mustn't add its own verticalScroll.
    VerticallyScrollableContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .widthIn(max = FormMaxWidth),
            verticalArrangement = Arrangement.spacedBy(20.dp),
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

            FormSection(title = stringResource(Res.string.balance_and_reference_section_title)) {
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

            // A new account is always open, so it can only be closed once it exists.
            if (state.isEditing) {
                FormSection(title = stringResource(Res.string.status_section_title)) {
                    ClosedField(isClosed = state.isClosed, enabled = enabled, onIsClosedChange = onIsClosedChange)
                }
            }

            FormSection(title = stringResource(Res.string.additional_information_section_title)) {
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
    FormSection(title = stringResource(Res.string.account_section_title)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(
                if (isEditing) Res.string.edit_account_description else Res.string.new_account_description
            ),
        )

        Text(
            text = stringResource(Res.string.required_field_legend),
            color = JewelTheme.globalColors.text.info,
        )
    }
}

private val FormMaxWidth = 600.dp
