package com.munzenberger.money.desktop.components

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.state.LoadState
import javafx.css.PseudoClass
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox

/**
 * A balance after its [label], if it has one: a progress indicator while it loads, the amount once it has, or an error
 * message if it can't be loaded or calculated. A negative amount is marked so the stylesheet can show it in red. Until
 * it's first rendered, it shows only its label.
 */
internal class AccountBalanceView(label: String? = null) : HBox(Spacing.CONTROL_SPACING) {
    private val label = label?.let(::Label)

    private val progress = smallProgressIndicator()

    // Negative amounts are in parentheses, in the accounting format.
    private val amount = Label().apply { styleClass += "balance-amount" }

    private val errorMessage = Label(strings.getString("account_balance_error_message")).apply {
        styleClass += "error-message"
    }

    init {
        alignment = Pos.CENTER_RIGHT
        children.setAll(listOfNotNull(this.label))
    }

    fun render(balance: LoadState<Money>) {
        val value = when (balance) {
            LoadState.Loading -> progress
            LoadState.Error -> errorMessage
            is LoadState.Loaded -> amount.apply {
                text = balance.value.toString(accountFormat = true)
                pseudoClassStateChanged(NEGATIVE_PSEUDO_CLASS, balance.value.value < 0)
            }
        }
        children.setAll(listOfNotNull(label, value))
    }

    private companion object {
        val NEGATIVE_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("negative")
    }
}
