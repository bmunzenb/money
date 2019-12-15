package com.munzenberger.money.app.control

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.core.Money
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.paint.Color
import javafx.util.Callback

class MoneyAsyncTableCell<S> : AsyncTableCell<S, Money>() {

    override fun onComplete(value: Money) {
        super.onComplete(value)
        // TODO: replace with css that properly handles highlighted color
        textFill = when (value.value < 0) {
            true -> Color.RED
            else -> Color.BLACK
        }
    }
}

class MoneyAsyncTableCellFactory<S> : Callback<TableColumn<S, AsyncObject<Money>>, TableCell<S, AsyncObject<Money>>> {

    override fun call(param: TableColumn<S, AsyncObject<Money>>?) = MoneyAsyncTableCell<S>()
}
