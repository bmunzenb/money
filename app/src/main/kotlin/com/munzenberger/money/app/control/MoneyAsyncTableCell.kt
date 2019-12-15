package com.munzenberger.money.app.control

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.core.Money
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class MoneyAsyncTableCell<S> : AsyncTableCell<S, Money>() {

    companion object {
        private const val NEGATIVE_STYLE_CLASS = "money-negative"
    }

    override fun onComplete(value: Money) {
        super.onComplete(value)

        styleClass.remove(NEGATIVE_STYLE_CLASS)

        if (value.value < 0) {
            styleClass.add(NEGATIVE_STYLE_CLASS)
        }
    }
}

class MoneyAsyncTableCellFactory<S> : Callback<TableColumn<S, AsyncObject<Money>>, TableCell<S, AsyncObject<Money>>> {

    override fun call(param: TableColumn<S, AsyncObject<Money>>?) = MoneyAsyncTableCell<S>()
}
