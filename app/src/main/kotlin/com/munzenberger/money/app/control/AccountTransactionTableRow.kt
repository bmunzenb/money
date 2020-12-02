package com.munzenberger.money.app.control

import com.munzenberger.money.app.model.FXAccountTransaction
import com.munzenberger.money.core.TransactionStatus
import javafx.beans.binding.Bindings
import javafx.css.PseudoClass
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableRow
import javafx.scene.control.ToggleGroup
import javafx.scene.input.MouseButton
import java.time.LocalDate

class AccountTransactionTableRow(
        private val add: () -> Unit,
        private val edit: (FXAccountTransaction) -> Unit,
        private val delete: (FXAccountTransaction) -> Unit,
        private val markAs: (FXAccountTransaction, TransactionStatus) -> Unit
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

        val markAs = Menu("Mark As").apply {

            val statusGroup = ToggleGroup()

            val unreconciled = createStatusRadioMenuItem(TransactionStatus.UNRECONCILED, "Unreconciled").apply {
                toggleGroup = statusGroup
            }

            val cleared = createStatusRadioMenuItem(TransactionStatus.CLEARED, "Cleared (C)").apply {
                toggleGroup = statusGroup
            }

            val reconciled = createStatusRadioMenuItem(TransactionStatus.RECONCILED, "Reconciled (R)").apply {
                toggleGroup = statusGroup
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

    override fun updateItem(item: FXAccountTransaction?, empty: Boolean) {
        super.updateItem(item, empty)

        val isFuture = !empty && item != null && item.dateProperty.value.isAfter(LocalDate.now())
        pseudoClassStateChanged(futureDatePseudoClass, isFuture)
    }

    private fun createStatusRadioMenuItem(status: TransactionStatus, text: String) =
            RadioMenuItem(text).apply {
                setOnAction { markAs(item, status) }
                // TODO bind the selected item property
            }
}
