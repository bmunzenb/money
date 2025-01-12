package com.munzenberger.money.app.control

import javafx.scene.control.Hyperlink
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class HyperlinkTableCell<S, T>(
    private val toString: (T) -> String = { it.toString() },
    private val action: (S) -> Unit,
) : TableCell<S, T>() {
    private val hyperlink =
        Hyperlink().apply {
            setOnAction {
                val item = tableRow.item
                action.invoke(item)
            }
        }

    override fun updateItem(
        item: T,
        empty: Boolean,
    ) {
        super.updateItem(item, empty)

        when {
            empty || item == null -> graphic = null
            else ->
                graphic =
                    hyperlink.apply {
                        text = toString.invoke(item)
                    }
        }
    }
}

class HyperlinkTableCellFactory<S, T>(
    private val toString: (T) -> String = { it.toString() },
    private val action: (S) -> Unit,
) : Callback<TableColumn<S, T>, TableCell<S, T>> {
    override fun call(param: TableColumn<S, T>?) = HyperlinkTableCell(toString, action)
}
