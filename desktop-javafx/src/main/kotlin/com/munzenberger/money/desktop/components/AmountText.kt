package com.munzenberger.money.desktop.components

import com.munzenberger.money.data.api.Money
import javafx.css.PseudoClass
import javafx.scene.control.Labeled

private val NEGATIVE_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("negative")

/**
 * Shows [amount] in the accounting format, with or without its currency symbol, and marks it negative if it's below
 * zero, so the stylesheet's `balance-amount` style shows it in red. Null shows nothing.
 */
internal fun Labeled.showAmount(amount: Money?, asCurrency: Boolean = true) {
    if ("balance-amount" !in styleClass) styleClass += "balance-amount"
    text = amount?.toString(accountFormat = true, asCurrency = asCurrency)
    pseudoClassStateChanged(NEGATIVE_PSEUDO_CLASS, amount != null && amount.value < 0)
}
