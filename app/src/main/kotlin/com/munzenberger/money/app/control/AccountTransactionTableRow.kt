package com.munzenberger.money.app.control

import com.munzenberger.money.app.model.FXAccountTransaction
import javafx.beans.binding.Bindings
import javafx.css.PseudoClass
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableRow
import javafx.scene.input.MouseButton
import java.time.LocalDate

class AccountTransactionTableRow(
        private val add: () -> Unit,
        private val edit: (FXAccountTransaction) -> Unit,
        private val delete: (FXAccountTransaction) -> Unit
) : TableRow<FXAccountTransaction>() {

    companion object {
        private val futureDatePseudoClass: PseudoClass = PseudoClass.getPseudoClass("future-date")
    }

    private val rowContextMenu = ContextMenu().apply {

        val edit = MenuItem("Edit").apply {
            setOnAction { edit(item) }
        }

        val delete = MenuItem("Delete").apply {
            setOnAction { delete(item) }
        }

        items.addAll(edit, SeparatorMenuItem(), delete)
    }

    init {
        setOnMouseClicked { event ->
            when {
                event.button == MouseButton.PRIMARY && event.clickCount == 2 -> when (item) {
                    null -> add()
                    else -> edit(item)
                }
            }
        }

        // show context menu only on rows with items
        contextMenuProperty().bind(
                Bindings.`when`(itemProperty().isNotNull)
                .then(rowContextMenu)
                .otherwise(null as ContextMenu?)
        )
    }

    override fun updateItem(item: FXAccountTransaction?, empty: Boolean) {
        super.updateItem(item, empty)

        val isFuture = !empty && item != null && item.dateProperty.value.isAfter(LocalDate.now())
        pseudoClassStateChanged(futureDatePseudoClass, isFuture)
    }
}
