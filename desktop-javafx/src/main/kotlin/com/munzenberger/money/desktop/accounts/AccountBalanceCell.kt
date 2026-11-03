package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.state.LoadState
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TableCell

/**
 * Shows an account's balance, or a subtotal after its label, in an [AccountBalanceView]. A group's row has no balance,
 * so its cell is empty.
 */
internal class AccountBalanceCell : TableCell<AccountListRow, LoadState<Money>?>() {
    private val accountBalance = AccountBalanceView()

    private val subtotal = AccountBalanceView(strings.getString("subtotal_label"))

    init {
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
    }

    override fun updateItem(item: LoadState<Money>?, empty: Boolean) {
        super.updateItem(item, empty)
        text = null
        graphic = when {
            item == null -> null
            tableRow?.item is AccountListRow.SubtotalRow -> subtotal.apply { render(item) }
            else -> accountBalance.apply { render(item) }
        }
    }
}
