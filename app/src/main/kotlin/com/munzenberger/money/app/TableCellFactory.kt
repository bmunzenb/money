package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class TableCellFactory<S, T>(private val onItem: (TableCell<S, T>, T?) -> Unit) : Callback<TableColumn<S, T>, TableCell<S, T>> {

    companion object {

        fun <S, T> text(block: (T?) -> String?) = TableCellFactory<S, T> { tableCell, item ->
            tableCell.text = block.invoke(item)
            tableCell.graphic = null
        }

        fun <S, T> asyncObject(block: (T) -> String?) = TableCellFactory<S, AsyncObject<T>> { tableCell, t ->

            when (t) {

                is AsyncObject.Error -> {
                    tableCell.text = t.error.message
                    tableCell.graphic = null
                }

                is AsyncObject.Complete -> {
                    tableCell.text = block.invoke(t.value)
                    tableCell.graphic = null
                }

                else -> {
                    tableCell.text = null
                    tableCell.graphic = ProgressIndicator().apply {
                        maxWidth = 20.0
                        maxHeight = 20.0
                    }
                }
            }
        }
    }

    override fun call(param: TableColumn<S, T>?) = object : TableCell<S, T>() {
        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty || item == null) onEmpty(this)
            else onItem.invoke(this, item)
        }
    }

    private fun onEmpty(tableCell: TableCell<S, T>) {
        tableCell.text = null
        tableCell.graphic = null
    }
}
