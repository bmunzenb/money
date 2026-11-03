package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.desktop.components.smallProgressIndicator
import com.munzenberger.money.desktop.getString
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.state.LoadState
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.TableCell

/**
 * Shows an account's balance, or a subtotal after its label: a progress indicator while it loads, the amount once it
 * has, or an error message if it can't be loaded. A group's row has no balance, so its cell is empty.
 */
internal class AccountBalanceCell : TableCell<AccountListRow, LoadState<Money>?>() {
    private val progress = smallProgressIndicator()

    private val errorMessage = balanceErrorMessage()

    init {
        contentDisplay = ContentDisplay.RIGHT
    }

    override fun updateItem(item: LoadState<Money>?, empty: Boolean) {
        super.updateItem(item, empty)
        val isSubtotal = tableRow?.item is AccountListRow.SubtotalRow
        val label = if (isSubtotal) strings.getString("subtotal_label") else null
        when (item) {
            null -> {
                text = null
                graphic = null
            }
            LoadState.Loading -> {
                text = label
                graphic = progress
            }
            LoadState.Error -> {
                text = label
                graphic = errorMessage
            }
            is LoadState.Loaded -> {
                val amount = item.value.toAccountString()
                text = if (isSubtotal) strings.getString("subtotal_value", amount) else amount
                graphic = null
            }
        }
    }
}

/** This amount as the account list shows it, with negative amounts in parentheses. */
internal fun Money.toAccountString(): String = toString(accountFormat = true)

/** The message shown in place of a balance that can't be loaded or calculated. */
internal fun balanceErrorMessage() = Label(strings.getString("account_balance_error_message")).apply {
    styleClass += "error-message"
}
