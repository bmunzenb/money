package com.munzenberger.money.app.control

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback

class TextListCell<T>(private val toString: (T) -> String? = { it.toString() }) : ListCell<T>() {

    override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)

        when {
            empty || item == null -> onEmpty()
            else -> onItem(item)
        }
    }

    private fun onEmpty() {
        text = null
        graphic = null
    }

    private fun onItem(item: T) {
        text = toString.invoke(item)
        graphic = null
    }
}

class TextListCellFactory<T>(private val toString: (T) -> String? = { it.toString() }) : Callback<ListView<T>, ListCell<T>> {

    override fun call(param: ListView<T>?) = TextListCell(toString)
}