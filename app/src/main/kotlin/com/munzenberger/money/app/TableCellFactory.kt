package com.munzenberger.money.app

import javafx.event.EventHandler
import javafx.scene.control.Hyperlink
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

abstract class TableCellFactory<S, T> : Callback<TableColumn<S, T>, TableCell<S, T>> {

    companion object {
        fun <S, T> text(block: (T) -> String? = { it.toString() }) = object : TableCellFactory<S, T>() {
            override fun onItem(tableCell: TableCell<S, T>, item: T) {
                tableCell.text = block.invoke(item)
                tableCell.graphic = null
            }
        }

        fun <S, T> hyperlink(block: (T) -> String? = { it.toString() }, action: (T) -> Unit) = object : TableCellFactory<S, T>() {
            override fun onItem(tableCell: TableCell<S, T>, item: T) {
                tableCell.text = null
                tableCell.graphic = Hyperlink(block.invoke(item)).apply {
                    onAction = EventHandler { _ -> action.invoke(item) }
                }
            }
        }
    }

    override fun call(param: TableColumn<S, T>?) = object : TableCell<S, T>() {
        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty || item == null) onEmpty(this)
            else onItem(this, item)
        }
    }

    abstract fun onItem(tableCell: TableCell<S, T>, item: T)

    open fun onEmpty(tableCell: TableCell<S, T>) {
        tableCell.text = null
        tableCell.graphic = null
    }
}
