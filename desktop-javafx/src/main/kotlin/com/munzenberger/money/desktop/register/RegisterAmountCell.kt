package com.munzenberger.money.desktop.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.desktop.components.showAmount
import javafx.scene.control.TableCell

/**
 * Shows an entry's decrease or increase, or the account's balance after it, in the accounting format without a
 * currency symbol. A negative amount, which only a balance can be, is marked so the stylesheet shows it in red, as it
 * does a negative balance elsewhere.
 */
internal class RegisterAmountCell : TableCell<RegisterEntry, Money?>() {
    override fun updateItem(item: Money?, empty: Boolean) {
        super.updateItem(item, empty)
        showAmount(item, asCurrency = false)
    }
}
