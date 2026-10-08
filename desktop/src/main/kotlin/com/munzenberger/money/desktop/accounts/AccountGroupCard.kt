package com.munzenberger.money.desktop.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_group_all_title
import money.shared.generated.resources.account_group_no_bank_title
import money.shared.generated.resources.account_list_empty_message
import money.shared.generated.resources.account_name_column_title
import money.shared.generated.resources.account_number_column_title
import money.shared.generated.resources.balance_column_title
import money.shared.generated.resources.collapse_account_group_action
import money.shared.generated.resources.expand_account_group_action
import money.shared.generated.resources.total_balance_row_title
import org.jetbrains.compose.resources.stringResource

/**
 * A card showing an [AccountGroup]: a header with the group's name, which toggles whether the group's
 * accounts are shown, above a table of the accounts with their name, masked account number, and balance.
 * Clicking an account's row calls [onAccountClick] with it. A footer row below the accounts shows the
 * group's total balance.
 */
@Composable
fun AccountGroupCard(
    group: AccountGroup,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAccountClick: (Account) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MoneyTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        AccountGroupHeader(
            title = group.title(),
            expanded = expanded,
            onClick = { onExpandedChange(!expanded) },
        )

        AnimatedVisibility(visible = expanded) {
            AccountTable(accounts = group.accounts, onAccountClick = onAccountClick)
        }
    }
}

@Composable
private fun AccountGroupHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val iconRotation by animateFloatAsState(targetValue = if (expanded) EXPANDED_ICON_ROTATION else 0f)
    val clickLabel = if (expanded) Res.string.collapse_account_group_action else Res.string.expand_account_group_action

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(clickLabel),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(MoneyTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.small),
    ) {
        Text(
            text = title,
            style = MoneyTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = null,
            modifier = Modifier.rotate(iconRotation),
        )
    }
}

@Composable
private fun AccountTable(
    accounts: List<Account>,
    onAccountClick: (Account) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (accounts.isEmpty()) {
            Text(
                text = stringResource(Res.string.account_list_empty_message),
                style = MoneyTheme.typography.bodyMedium,
                color = MoneyTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = MoneyTheme.spacing.medium,
                    end = MoneyTheme.spacing.medium,
                    bottom = MoneyTheme.spacing.small,
                ),
            )
            return@Column
        }

        val headerStyle = MoneyTheme.typography.labelMedium.copy(color = MoneyTheme.colorScheme.onSurfaceVariant)
        AccountTableRow(
            name = stringResource(Res.string.account_name_column_title),
            number = stringResource(Res.string.account_number_column_title),
            balance = stringResource(Res.string.balance_column_title),
            style = headerStyle,
            // Spans the card's width, so the column headers stand out as a band above the accounts.
            modifier = Modifier.background(MoneyTheme.colorScheme.surfaceContainer),
        )

        accounts.forEachIndexed { index, account ->
            if (index > 0) {
                HorizontalDivider(color = MoneyTheme.colorScheme.surfaceContainer)
            }
            AccountTableRow(
                name = account.name,
                number = maskAccountNumber(account.number),
                // The balance isn't available yet.
                balance = "",
                style = MoneyTheme.typography.bodyMedium,
                // clickable's indication highlights the row on hover, as well as on press and focus.
                modifier = Modifier.clickable(role = Role.Button) { onAccountClick(account) },
            )
        }

        AccountTableRow(
            name = stringResource(Res.string.total_balance_row_title),
            number = "",
            // The total balance isn't available yet.
            balance = "",
            style = MoneyTheme.typography.labelLarge,
            // A band like the column headers', closing off the table at the bottom of the card.
            modifier = Modifier.background(MoneyTheme.colorScheme.surfaceContainer),
        )
    }
}

/** A row of the account table, used for the column headers, the accounts, and the total, so their columns line up. */
@Composable
private fun AccountTableRow(
    name: String,
    number: String,
    balance: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MoneyTheme.spacing.medium, vertical = MoneyTheme.spacing.small),
        horizontalArrangement = Arrangement.spacedBy(MoneyTheme.spacing.medium),
    ) {
        AccountTableCell(text = name, style = style, weight = NAME_COLUMN_WEIGHT)
        AccountTableCell(text = number, style = style, weight = NUMBER_COLUMN_WEIGHT)
        AccountTableCell(text = balance, style = style, weight = BALANCE_COLUMN_WEIGHT, textAlign = TextAlign.End)
    }
}

@Composable
private fun RowScope.AccountTableCell(
    text: String,
    style: TextStyle,
    weight: Float,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun AccountGroup.title(): String = when (this) {
    is AccountGroup.All -> stringResource(Res.string.account_group_all_title)
    is AccountGroup.ByAccountType -> accountType.value.label()
    is AccountGroup.ByAccountClass -> accountClass.value.label()
    is AccountGroup.ByBank -> bank?.name ?: stringResource(Res.string.account_group_no_bank_title)
}

private const val EXPANDED_ICON_ROTATION = 180f
private const val NAME_COLUMN_WEIGHT = 2f
private const val NUMBER_COLUMN_WEIGHT = 1f
private const val BALANCE_COLUMN_WEIGHT = 1f

@Preview
@Composable
private fun AccountGroupCardPreview() {
    PreviewThemed {
        var expanded by remember { mutableStateOf(true) }
        AccountGroupCard(
            group = AccountGroup.ByBank(
                bank = Bank(name = "First National Bank"),
                accounts = listOf(
                    Account(name = "Checking", number = "123456789", accountType = previewAccountType),
                    Account(name = "Savings", number = "987654321", accountType = previewAccountType),
                    Account(name = "Holiday fund", accountType = previewAccountType),
                ),
            ),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onAccountClick = {},
            modifier = Modifier.padding(MoneyTheme.spacing.medium),
        )
    }
}

@Preview
@Composable
private fun AccountGroupCardCollapsedPreview() {
    PreviewThemed {
        AccountGroupCard(
            group = AccountGroup.ByBank(
                bank = null,
                accounts = listOf(Account(name = "Wallet", accountType = previewAccountType)),
            ),
            expanded = false,
            onExpandedChange = {},
            onAccountClick = {},
            modifier = Modifier.padding(MoneyTheme.spacing.medium),
        )
    }
}

@Preview
@Composable
private fun AccountGroupCardEmptyPreview() {
    PreviewThemed {
        AccountGroupCard(
            group = AccountGroup.All(accounts = emptyList()),
            expanded = true,
            onExpandedChange = {},
            onAccountClick = {},
            modifier = Modifier.padding(MoneyTheme.spacing.medium),
        )
    }
}

private val previewAccountType = AccountType(
    id = AccountTypeId(1),
    accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
    value = AccountTypeConstant.Checking,
)
