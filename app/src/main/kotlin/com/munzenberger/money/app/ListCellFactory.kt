package com.munzenberger.money.app

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback

class ListCellFactory<T>(private val onItem: (ListCell<T>, T) -> Unit) : Callback<ListView<T>, ListCell<T>> {

    companion object {
        fun <T> string(block: (T) -> String?) = ListCellFactory<T> { listCell, item ->
            listCell.text = block.invoke(item)
            listCell.graphic = null
        }
    }

    override fun call(param: ListView<T>?) = object : ListCell<T>() {
        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty) onEmpty(this)
            else onItem.invoke(this, item!!)
        }
    }

    private fun onEmpty(listCell: ListCell<T>) {
        listCell.text = null
        listCell.graphic = null
    }
}
