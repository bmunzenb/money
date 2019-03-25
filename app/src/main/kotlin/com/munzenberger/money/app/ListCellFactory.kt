package com.munzenberger.money.app

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback

abstract class ListCellFactory<T> : Callback<ListView<T>, ListCell<T>> {

    companion object {
        fun <T> text(block: (T) -> String? = { it.toString() }) = object : ListCellFactory<T>() {
            override fun onItem(listCell: ListCell<T>, item: T) {
                listCell.text = block.invoke(item)
                listCell.graphic = null
            }
        }
    }

    override fun call(param: ListView<T>?) = object : ListCell<T>() {
        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty || item == null) onEmpty(this)
            else onItem(this, item)
        }
    }

    abstract fun onItem(listCell: ListCell<T>, item: T)

    open fun onEmpty(listCell: ListCell<T>) {
        listCell.text = null
        listCell.graphic = null
    }
}
