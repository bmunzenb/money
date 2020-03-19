package com.munzenberger.money.app.control

import com.munzenberger.money.core.Money
import com.munzenberger.money.core.isNegative
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class MoneyTableCell<S> : TableCell<S, Money>() {

    companion object {
        const val NEGATIVE_STYLE_CLASS = "money-negative"
    }

    override fun updateItem(value: Money?, empty: Boolean) {
        super.updateItem(item, empty)

        styleClass.remove(NEGATIVE_STYLE_CLASS)

        when (value) {
            null -> text = null
            else -> {
                text = value.toString()
                when {
                    value.isNegative -> styleClass.add(NEGATIVE_STYLE_CLASS)
                }
            }
        }
    }
}

class MoneyTableCellFactory<S>() : Callback<TableColumn<S, Money>, TableCell<S, Money>> {

    override fun call(param: TableColumn<S, Money>?) = MoneyTableCell<S>()
}
