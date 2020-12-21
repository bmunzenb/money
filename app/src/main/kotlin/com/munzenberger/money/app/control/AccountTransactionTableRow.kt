package com.munzenberger.money.app.control

import com.munzenberger.money.app.model.FXRegisterEntry
import com.munzenberger.money.core.TransactionStatus
import javafx.beans.binding.Bindings
import javafx.css.PseudoClass
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableRow
import javafx.scene.input.MouseButton
import java.time.LocalDate

class AccountTransactionTableRow(
        private val add: () -> Unit,
        private val edit: (FXRegisterEntry) -> Unit,
        private val delete: (FXRegisterEntry) -> Unit,
        private val markAs: (FXRegisterEntry, TransactionStatus) -> Unit
) : TableRow<FXRegisterEntry>() {

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

        val markAs = Menu("Mark As").apply {

            //val statusGroup = ToggleGroup()

            val unreconciled = createStatusMenuItem(TransactionStatus.UNRECONCILED, "Unreconciled").apply {
                //toggleGroup = statusGroup
            }

            val cleared = createStatusMenuItem(TransactionStatus.CLEARED, "Cleared (C)").apply {
                //toggleGroup = statusGroup
            }

            val reconciled = createStatusMenuItem(TransactionStatus.RECONCILED, "Reconciled (R)").apply {
                //toggleGroup = statusGroup
            }

            items.addAll(unreconciled, cleared, reconciled)
        }

        items.addAll(
                edit,
                SeparatorMenuItem(),
                markAs,
                SeparatorMenuItem(),
                delete
        )
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

    override fun updateItem(item: FXRegisterEntry?, empty: Boolean) {
        super.updateItem(item, empty)

        val isFuture = !empty && item != null && item.dateProperty.value.isAfter(LocalDate.now())
        pseudoClassStateChanged(futureDatePseudoClass, isFuture)
    }

    private fun createStatusMenuItem(status: TransactionStatus, text: String) =
            // TODO convert to RadioMenuItem and bind the selected property to the item's status
            MenuItem(text).apply {
                setOnAction { markAs(item, status) }
            }
}
