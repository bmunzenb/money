package com.munzenberger.money.app.control

import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class TableCellFactory<S, T>(
        val toString: (T) -> String
) : Callback<TableColumn<S, T>, TableCell<S, T>> {

    override fun call(param: TableColumn<S, T>?) = object : TableCell<S, T>() {

        override fun updateItem(item: T, empty: Boolean) {
            super.updateItem(item, empty)

            text = when (empty) {
                true -> null
                else -> toString(item)
            }
        }
    }
}
