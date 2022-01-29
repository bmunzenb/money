package com.munzenberger.money.app.control

import javafx.beans.value.ChangeListener
import javafx.scene.control.CheckBox
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class CheckBoxTableCell<S, T>(
        private val isChecked: (T) -> Boolean,
        onChanged: (S, Boolean) -> Unit
) : TableCell<S, T>() {

    private val checkbox = CheckBox()

    private val listener = ChangeListener<Boolean> { _, _, newValue ->
        onChanged(tableRow.item, newValue)
    }

    override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)

        // disable the listener while the cell is updated
        checkbox.selectedProperty().removeListener(listener)

        graphic = when {
            empty || item == null -> null
            else -> checkbox.apply {
                isSelected = isChecked(item)
            }
        }

        // re-enable to listener when done updating the cell
        checkbox.selectedProperty().addListener(listener)
    }
}

class CheckBoxTableViewCellFactory<S, T>(
        private val isChecked: (T) -> Boolean,
        private val onChanged: (S, Boolean) -> Unit
) : Callback<TableColumn<S, T>, TableCell<S, T>> {

    override fun call(param: TableColumn<S, T>?): TableCell<S, T> = CheckBoxTableCell(isChecked, onChanged)
}
