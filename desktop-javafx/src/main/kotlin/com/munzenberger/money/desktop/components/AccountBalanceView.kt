package com.munzenberger.money.desktop.components

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.state.LoadState
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox

/**
 * A balance after its [label], if it has one: a progress indicator while it loads, the amount once it has, or an error
 * message if it can't be loaded or calculated. A negative amount is marked so the stylesheet can show it in red. It
 * starts out loading.
 */
internal class AccountBalanceView(label: String? = null) : HBox(Spacing.CONTROL_SPACING) {
    private val label = label?.let(::Label)

    private val progress = smallProgressIndicator()

    // Negative amounts are in parentheses, in the accounting format.
    private val amount = Label()

    private val errorMessage = Label(strings.getString("account_balance_error_message")).apply {
        styleClass += "error-message"
    }

    init {
        alignment = Pos.CENTER_RIGHT
        render(LoadState.Loading)
    }

    fun render(balance: LoadState<Money>) {
        val value = when (balance) {
            LoadState.Loading -> progress
            LoadState.Error -> errorMessage
            is LoadState.Loaded -> amount.apply { showAmount(balance.value) }
        }
        children.setAll(listOfNotNull(label, value))
    }
}
