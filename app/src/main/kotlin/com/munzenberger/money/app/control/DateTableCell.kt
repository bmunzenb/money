package com.munzenberger.money.app.control

import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class DateTableCell<S>(style: Int = DateFormat.SHORT) : TableCell<S, Date>() {

    private val formatter = DateFormat.getDateInstance(style, Locale.getDefault())

    override fun updateItem(item: Date?, empty: Boolean) {
        super.updateItem(item, empty)

        text = when {
            empty || item == null -> null
            else -> formatter.format(item)
        }
    }
}

class DateTableCellFactory<S>(
        private val style: Int = DateFormat.SHORT
) : Callback<TableColumn<S, Date>, TableCell<S, Date>> {

    override fun call(param: TableColumn<S, Date>?) = DateTableCell<S>(style)
}