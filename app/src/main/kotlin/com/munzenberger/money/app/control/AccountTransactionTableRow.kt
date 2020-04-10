package com.munzenberger.money.app.control

import com.munzenberger.money.app.model.FXAccountTransaction
import javafx.css.PseudoClass
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableRow
import javafx.scene.input.MouseButton
import java.time.LocalDate

class AccountTransactionTableRow(
        private val addTransaction: () -> Unit,
        private val editTransaction: (FXAccountTransaction) -> Unit,
        private val deleteTransactions: (List<FXAccountTransaction>) -> Unit
) : TableRow<FXAccountTransaction>() {

    companion object {
        private val futureDatePseudoClass: PseudoClass = PseudoClass.getPseudoClass("future-date")
    }

    init {
        setOnMouseClicked { event ->
            when {
                event.button == MouseButton.PRIMARY && event.clickCount == 2 -> when (item) {
                    null -> addTransaction()
                    else -> editTransaction(item)
                }
            }
        }
    }

    override fun updateItem(item: FXAccountTransaction?, empty: Boolean) {
        super.updateItem(item, empty)

        contextMenu = when {
            empty || item == null -> null
            else -> ContextMenu().apply {
                items.addAll(
                        MenuItem("Edit").apply { setOnAction { editTransaction(item) } },
                        SeparatorMenuItem(),
                        MenuItem("Delete").apply { setOnAction { deleteTransactions(listOf(item)) } }
                )
            }
        }

        val isFuture = !empty && item != null && item.dateProperty.value.isAfter(LocalDate.now())
        pseudoClassStateChanged(futureDatePseudoClass, isFuture)
    }
}