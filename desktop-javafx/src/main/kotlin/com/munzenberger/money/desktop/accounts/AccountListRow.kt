package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.data.api.account.Account

/** A row of the account list's table: a group, then a row for each of its accounts, then its subtotal. */
sealed interface AccountListRow {
    data class GroupRow(val group: AccountGroup) : AccountListRow

    data class AccountRow(val account: Account) : AccountListRow

    data class SubtotalRow(val group: AccountGroup) : AccountListRow
}
