package com.munzenberger.money.app.control

import com.munzenberger.money.app.model.moneyNegativePseudoClass
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.isNegative
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class MoneyTableCell<S>(
        private val withCurrency: Boolean,
        private val negativeStyle: Boolean
) : TableCell<S, Money>() {

    override fun updateItem(value: Money?, empty: Boolean) {
        super.updateItem(item, empty)

        text = value?.text(withCurrency)

        if (negativeStyle) {
            val isMoneyNegative = !empty && value != null && value.isNegative
            pseudoClassStateChanged(moneyNegativePseudoClass, isMoneyNegative)
        }
    }
}

class MoneyTableCellFactory<S>(
        private val withCurrency: Boolean = true,
        private val negativeStyle: Boolean = true
) : Callback<TableColumn<S, Money>, TableCell<S, Money>> {

    override fun call(param: TableColumn<S, Money>?) =
            MoneyTableCell<S>(withCurrency, negativeStyle)
}

private fun Money.text(withCurrency: Boolean) = when (withCurrency) {
    true -> toString()
    else -> toStringWithoutCurrency()
}
