package com.munzenberger.money.app.control

import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class DateTableCell<S>(style: FormatStyle) : TableCell<S, LocalDate>() {

    private val formatter = DateTimeFormatter.ofLocalizedDate(style)

    override fun updateItem(item: LocalDate?, empty: Boolean) {
        super.updateItem(item, empty)

        text = when {
            empty || item == null -> null
            else -> item.format(formatter)
        }
    }
}

class DateTableCellFactory<S>(
        private val style: FormatStyle = FormatStyle.SHORT
) : Callback<TableColumn<S, LocalDate>, TableCell<S, LocalDate>> {

    override fun call(param: TableColumn<S, LocalDate>?) = DateTableCell<S>(style)
}
