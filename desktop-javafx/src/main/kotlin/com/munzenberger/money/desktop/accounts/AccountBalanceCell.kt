package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.accounts.AccountBalances
import com.munzenberger.money.presentation.state.LoadState
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TableCell

/**
 * A balance column cell's value: an account's balance or a group's subtotal. They're different types, so a cell whose
 * row changes between an account and a subtotal shows the change even when the two balances are equal.
 */
internal sealed interface BalanceCellValue {
    val balance: LoadState<Money>

    data class AccountBalance(override val balance: LoadState<Money>) : BalanceCellValue

    data class Subtotal(override val balance: LoadState<Money>) : BalanceCellValue
}

/** This row's value in the balance column from [balances], or null for a group's row, which has no balance. */
internal fun AccountListRow.balanceCellValue(balances: AccountBalances): BalanceCellValue? = when (this) {
    is AccountListRow.GroupRow -> null
    is AccountListRow.AccountRow -> BalanceCellValue.AccountBalance(balances.balance(account))
    is AccountListRow.SubtotalRow -> BalanceCellValue.Subtotal(balances.subtotal(group))
}

/**
 * Shows an account's balance, or a subtotal after its label, in an [AccountBalanceView]. A group's row has no balance,
 * so its cell is empty.
 */
internal class AccountBalanceCell : TableCell<AccountListRow, BalanceCellValue?>() {
    private val accountBalance = AccountBalanceView()

    private val subtotal = AccountBalanceView(strings.getString("subtotal_label"))

    init {
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
    }

    override fun updateItem(item: BalanceCellValue?, empty: Boolean) {
        super.updateItem(item, empty)
        text = null
        graphic = when (item) {
            null -> null
            is BalanceCellValue.AccountBalance -> accountBalance.apply { render(item.balance) }
            is BalanceCellValue.Subtotal -> subtotal.apply { render(item.balance) }
        }
    }
}
