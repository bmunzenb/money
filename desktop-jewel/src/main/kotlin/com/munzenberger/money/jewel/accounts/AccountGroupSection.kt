package com.munzenberger.money.jewel.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.jewel.theme.PreviewThemed
import com.munzenberger.money.presentation.accounts.label
import com.munzenberger.money.presentation.accounts.maskAccountNumber
import money.presentation.generated.resources.Res
import money.presentation.generated.resources.account_group_all_title
import money.presentation.generated.resources.account_group_no_bank_title
import money.presentation.generated.resources.collapse_account_group_action
import money.presentation.generated.resources.expand_account_group_action
import money.presentation.generated.resources.total_balance_row_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.simpleListItemStyle

/**
 * An [AccountGroup] as a node of a tree, like the IDE's project view: a header row with a chevron and
 * the group's name, which toggles whether the group's accounts are shown below it, indented. Each
 * account's row shows its name, masked account number, and balance, and calls [onAccountClick] with
 * it when clicked. A closed account's row is dimmed. A row below the accounts shows the group's total
 * balance.
 */
@Composable
fun AccountGroupSection(
    group: AccountGroup,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAccountClick: (Account) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AccountGroupHeader(
            title = group.title(),
            expanded = expanded,
            onClick = { onExpandedChange(!expanded) },
        )

        if (expanded) {
            group.accounts.forEach { account ->
                AccountRow(account = account, onClick = { onAccountClick(account) })
            }
            AccountGroupTotalRow()
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun AccountGroupHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val clickLabel = if (expanded) Res.string.collapse_account_group_action else Res.string.expand_account_group_action

    TreeRow(
        onClickLabel = stringResource(clickLabel),
        onClick = onClick,
        startPadding = 6.dp,
    ) {
        Icon(
            key = if (expanded) AllIconsKeys.General.ChevronDown else AllIconsKeys.General.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
    }
}

@Composable
private fun AccountRow(account: Account, onClick: () -> Unit) {
    // Dimmed, but still readable, so a closed account recedes without looking disabled.
    val color = if (account.isClosed) JewelTheme.globalColors.text.info else Color.Unspecified

    TreeRow(onClick = onClick, startPadding = AccountIndent) {
        Text(
            text = account.name,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = maskAccountNumber(account.number),
            color = JewelTheme.globalColors.text.info,
            maxLines = 1,
            modifier = Modifier.width(NumberColumnWidth),
        )
        Text(
            // The balance isn't available yet.
            text = "",
            color = color,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(BalanceColumnWidth),
        )
    }
}

/** The group's total balance, below its accounts, with the total in the accounts' balance column. */
@Composable
private fun AccountGroupTotalRow() {
    TreeRow(startPadding = AccountIndent) {
        Text(
            text = stringResource(Res.string.total_balance_row_title),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(NumberColumnWidth))
        Text(
            // The total balance isn't available yet.
            text = "",
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(BalanceColumnWidth),
        )
    }
}

/**
 * A row of the tree, used for the group header, the accounts, and the total, so their columns line up.
 * It's highlighted on hover if it has an [onClick], since Jewel's theme has no click indication.
 */
@Composable
private fun TreeRow(
    startPadding: Dp,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background = if (onClick != null && hovered) {
        JewelTheme.simpleListItemStyle.colors.backgroundSelected
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .background(background, RowShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClickLabel = onClickLabel,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            )
            .padding(start = startPadding, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun AccountGroup.title(): String = when (this) {
    is AccountGroup.All -> stringResource(Res.string.account_group_all_title)
    is AccountGroup.ByAccountType -> accountType.value.label()
    is AccountGroup.ByAccountClass -> accountClass.value.label()
    is AccountGroup.ByBank -> bank?.name ?: stringResource(Res.string.account_group_no_bank_title)
}

/** A key that identifies the group across emissions, so its section keeps its expanded state. */
internal fun AccountGroup.key(): String = when (this) {
    is AccountGroup.All -> "all"
    is AccountGroup.ByAccountType -> "type:${accountType.id.value}"
    is AccountGroup.ByAccountClass -> "class:${accountClass.id.value}"
    is AccountGroup.ByBank -> "bank:${bank?.id?.value}"
}

private val RowHeight = 28.dp
private val RowShape = RoundedCornerShape(4.dp)

// Lines the account names up with the group name, past the header's chevron.
private val AccountIndent = 30.dp
private val NumberColumnWidth = 140.dp
private val BalanceColumnWidth = 140.dp

@Preview
@Composable
private fun AccountGroupSectionPreview() {
    PreviewThemed {
        var expanded by remember { mutableStateOf(true) }
        AccountGroupSection(
            group = AccountGroup.ByBank(
                bank = Bank(name = "First National Bank"),
                accounts = listOf(
                    Account(name = "Checking", number = "123456789", accountType = previewAccountType),
                    Account(name = "Savings", number = "987654321", accountType = previewAccountType),
                    Account(name = "Holiday fund", accountType = previewAccountType),
                    Account(name = "Old savings", accountType = previewAccountType, isClosed = true),
                ),
            ),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onAccountClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview
@Composable
private fun AccountGroupSectionCollapsedPreview() {
    PreviewThemed {
        AccountGroupSection(
            group = AccountGroup.ByBank(
                bank = null,
                accounts = listOf(Account(name = "Wallet", accountType = previewAccountType)),
            ),
            expanded = false,
            onExpandedChange = {},
            onAccountClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

private val previewAccountType = AccountType(
    id = AccountTypeId(1),
    accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
    value = AccountTypeConstant.Checking,
)
