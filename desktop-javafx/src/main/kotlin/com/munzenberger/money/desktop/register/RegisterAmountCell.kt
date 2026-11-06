package com.munzenberger.money.desktop.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.register.RegisterEntry
import javafx.css.PseudoClass
import javafx.scene.control.TableCell

/**
 * Shows an entry's amount, or the account's balance after it, in the accounting format without a currency symbol. A
 * negative amount is marked so the stylesheet shows it in red, as it does a negative balance elsewhere.
 */
internal class RegisterAmountCell : TableCell<RegisterEntry, Money?>() {

    init {
        styleClass += "balance-amount"
    }

    override fun updateItem(item: Money?, empty: Boolean) {
        super.updateItem(item, empty)
        text = item?.toString(accountFormat = true, asCurrency = false)
        pseudoClassStateChanged(NEGATIVE_PSEUDO_CLASS, item != null && item.value < 0)
    }

    private companion object {
        val NEGATIVE_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("negative")
    }
}
