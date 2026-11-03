package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountTypeConstant

/** A row of the account list's table: a group, then a row for each of its accounts, then its subtotal. */
sealed interface AccountListRow {
    data class GroupRow(val group: AccountGroup) : AccountListRow

    data class AccountRow(val account: Account) : AccountListRow

    data class SubtotalRow(val group: AccountGroup) : AccountListRow
}

/**
 * A group's rows: its group row, then its accounts' rows, then its subtotal's row. The all accounts group has only its
 * accounts' rows.
 */
internal fun AccountGroup.rows(): List<AccountListRow> {
    val accountRows = accounts.map(AccountListRow::AccountRow)
    return if (this is AccountGroup.All) {
        accountRows
    } else {
        listOf(AccountListRow.GroupRow(this)) + accountRows + AccountListRow.SubtotalRow(this)
    }
}

/**
 * These rows sorted by [comparator]: the groups, and the accounts in each group, keeping each group's row first and its
 * subtotal's row last. Account type and account class groups stay in the order of their constants instead.
 */
internal fun List<AccountListRow>.sortedBySections(comparator: Comparator<AccountListRow>): List<AccountListRow> {
    val sections = sections()
    val sectionOrder: Comparator<List<AccountListRow>> = when {
        sections.all { it.first().accountType() != null } -> compareBy { it.first().accountType() }
        sections.all { it.first().accountClass() != null } -> compareBy { it.first().accountClass() }
        else -> compareBy(comparator) { it.first() }
    }
    val rowOrder = compareBy<AccountListRow> { it.sectionPosition() }.then(comparator)
    return sections.sortedWith(sectionOrder).flatMap { it.sortedWith(rowOrder) }
}

/**
 * Splits the rows into their groups' sections: each group's row, then its accounts' rows, then its subtotal's row. The
 * all accounts group has no group or subtotal row, so its section is just its accounts' rows.
 */
internal fun List<AccountListRow>.sections(): List<List<AccountListRow>> {
    val sections = mutableListOf<MutableList<AccountListRow>>()
    forEach { row ->
        if (row is AccountListRow.GroupRow || sections.isEmpty()) {
            sections += mutableListOf<AccountListRow>()
        }
        sections.last() += row
    }
    return sections
}

/**
 * Where the row goes in its group's section: the group's row first, then its accounts' rows, then its subtotal's row.
 */
internal fun AccountListRow.sectionPosition(): Int = when (this) {
    is AccountListRow.GroupRow -> 0
    is AccountListRow.AccountRow -> 1
    is AccountListRow.SubtotalRow -> 2
}

// The account type of an account type group's row, or null for any other row.
private fun AccountListRow.accountType(): AccountTypeConstant? =
    ((this as? AccountListRow.GroupRow)?.group as? AccountGroup.ByAccountType)?.accountType?.value

// The account class of an account class group's row, or null for any other row.
private fun AccountListRow.accountClass(): AccountClassConstant? =
    ((this as? AccountListRow.GroupRow)?.group as? AccountGroup.ByAccountClass)?.accountClass?.value
