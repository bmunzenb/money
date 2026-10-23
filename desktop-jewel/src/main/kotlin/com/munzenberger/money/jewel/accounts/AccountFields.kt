package com.munzenberger.money.jewel.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.jewel.components.FormField
import com.munzenberger.money.jewel.components.rememberSyncedTextFieldState
import com.munzenberger.money.presentation.accounts.LoadState
import com.munzenberger.money.presentation.accounts.label
import com.munzenberger.money.presentation.accounts.loadedOrEmpty
import money.presentation.generated.resources.Res
import money.presentation.generated.resources.account_closed_label
import money.presentation.generated.resources.account_name_error_message
import money.presentation.generated.resources.account_name_label
import money.presentation.generated.resources.account_number_label
import money.presentation.generated.resources.account_type_error_message
import money.presentation.generated.resources.account_type_label
import money.presentation.generated.resources.account_type_load_error_message
import money.presentation.generated.resources.comments_label
import money.presentation.generated.resources.financial_institution_label
import money.presentation.generated.resources.financial_institution_load_error_message
import money.presentation.generated.resources.initial_balance_error_message
import money.presentation.generated.resources.initial_balance_label
import money.presentation.generated.resources.required_field_label
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.EditableComboBox
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.SimpleListItem
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextArea
import org.jetbrains.jewel.ui.component.TextField

/** Required field for the account's name. */
@Composable
internal fun NameField(
    name: String,
    isError: Boolean,
    enabled: Boolean,
    onNameChange: (String) -> Unit,
) {
    val label = stringResource(Res.string.required_field_label, stringResource(Res.string.account_name_label))

    FormField(
        label = label,
        error = if (isError) stringResource(Res.string.account_name_error_message) else null,
    ) {
        TextField(
            state = rememberSyncedTextFieldState(value = name, onValueChange = onNameChange),
            enabled = enabled,
            outline = if (isError) Outline.Error else Outline.None,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
        )
    }
}

/**
 * Required combo box for picking the account type. It starts out empty, but there's no empty option in
 * the list, so once a type is picked it can only be changed to another type. The combo box is disabled
 * until the account types load, and shows an error if they can't be loaded, since the form can't be
 * completed without one, or if the user tries to save without picking one.
 */
@Composable
internal fun AccountTypeField(
    accountTypes: LoadState<List<AccountType>>,
    accountType: AccountType?,
    isMissing: Boolean,
    enabled: Boolean,
    onAccountTypeChange: (AccountType) -> Unit,
) {
    val label = stringResource(Res.string.required_field_label, stringResource(Res.string.account_type_label))
    val options = accountTypes.loadedOrEmpty
    val error = when {
        accountTypes is LoadState.Error -> stringResource(Res.string.account_type_load_error_message)
        isMissing -> stringResource(Res.string.account_type_error_message)
        else -> null
    }

    FormField(label = label, error = error) {
        ListComboBox(
            items = options.map { it.value.label() },
            // -1 when no type is picked yet, which shows an empty combo box.
            selectedIndex = options.indexOf(accountType),
            onSelectedItemChange = { onAccountTypeChange(options[it]) },
            enabled = enabled && accountTypes is LoadState.Loaded,
            outline = if (error != null) Outline.Error else Outline.None,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
        )
    }
}

/**
 * Optional editable combo box for the account's financial institution. The user can pick an existing
 * bank from the popup or type the name of a new one. As they type, the popup opens and narrows to the
 * banks whose names contain the text; once the text names an existing bank, the popup lists every bank
 * again so the user can switch to another one. If the banks can't be loaded, the field still takes a
 * typed name, and says that existing banks aren't available.
 */
@Composable
internal fun FinancialInstitutionField(
    banks: LoadState<List<Bank>>,
    bankName: String,
    bank: Bank?,
    enabled: Boolean,
    onBankNameChange: (String) -> Unit,
    onBankChange: (Bank) -> Unit,
) {
    val label = stringResource(Res.string.financial_institution_label)
    val popupManager = remember { PopupManager() }
    val allBanks = banks.loadedOrEmpty
    val options = if (bank != null) {
        allBanks
    } else {
        allBanks.filter { it.name.contains(bankName.trim(), ignoreCase = true) }
    }

    // An empty popup has nothing to pick, so it closes once typing has filtered out every bank.
    LaunchedEffect(options.isEmpty()) {
        if (options.isEmpty()) popupManager.setPopupVisible(false)
    }

    val error = if (banks is LoadState.Error) {
        stringResource(Res.string.financial_institution_load_error_message)
    } else {
        null
    }

    FormField(label = label, error = error) {
        EditableComboBox(
            textFieldState = rememberSyncedTextFieldState(
                value = bankName,
                onValueChange = {
                    onBankNameChange(it)
                    popupManager.setPopupVisible(true)
                },
            ),
            enabled = enabled,
            maxPopupHeight = BankPopupMaxHeight,
            popupManager = popupManager,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
            ) {
                options.forEach { option ->
                    BankOption(
                        bank = option,
                        selected = option == bank,
                        onClick = {
                            onBankChange(option)
                            popupManager.setPopupVisible(false)
                        },
                    )
                }
            }
        }
    }
}

/** A bank in the financial institution popup, highlighted when it's the picked bank or under the pointer. */
@Composable
private fun BankOption(bank: Bank, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    SimpleListItem(
        text = bank.name,
        selected = selected || hovered,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    )
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
    val label = stringResource(Res.string.account_number_label)

    FormField(label = label) {
        TextField(
            state = rememberSyncedTextFieldState(value = number, onValueChange = onNumberChange),
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
        )
    }
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
    val label = stringResource(Res.string.initial_balance_label)
    // onFocusChanged also reports the initial unfocused state, which isn't the user leaving the field.
    var hasFocus by remember { mutableStateOf(false) }

    FormField(
        label = label,
        error = if (isError) stringResource(Res.string.initial_balance_error_message) else null,
    ) {
        TextField(
            state = rememberSyncedTextFieldState(value = initialBalance, onValueChange = onInitialBalanceChange),
            enabled = enabled,
            outline = if (isError) Outline.Error else Outline.None,
            placeholder = { Text(Money(0).toString(asCurrency = false)) },
            leadingIcon = { Text(text = currencySymbol, modifier = Modifier.padding(end = 4.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label }
                .onFocusChanged {
                    if (hasFocus && !it.isFocused) onFocusLost()
                    hasFocus = it.isFocused
                },
        )
    }
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
    val label = stringResource(Res.string.comments_label)

    // The label lines up with the first line of text, not the middle of the area.
    FormField(label = label, verticalAlignment = Alignment.Top) {
        TextArea(
            state = rememberSyncedTextFieldState(value = memo, onValueChange = onMemoChange),
            enabled = enabled,
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3, maxHeightInLines = 6),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier
                .fillMaxWidth()
                // TextArea's scrollbar container needs a bounded height, which it doesn't get inside the
                // form's scroll area. It's only a cap: the area still sizes to its lines.
                .heightIn(max = CommentsMaxHeight)
                .semantics { contentDescription = label },
        )
    }
}

/** Checkbox for whether the account is closed. Clicking its label toggles it too, as in the IDE. */
@Composable
internal fun ClosedField(
    isClosed: Boolean,
    enabled: Boolean,
    onIsClosedChange: (Boolean) -> Unit,
) {
    CheckboxRow(
        text = stringResource(Res.string.account_closed_label),
        checked = isClosed,
        onCheckedChange = onIsClosedChange,
        enabled = enabled,
        modifier = Modifier.heightIn(min = 28.dp),
    )
}

private val BankPopupMaxHeight = 200.dp

// Comfortably more than the six lines lineLimits allows, so it never cuts the text area short.
private val CommentsMaxHeight = 200.dp
