package com.munzenberger.money.app.control

import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.core.TransactionStatus
import javafx.css.PseudoClass
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableRow
import javafx.scene.input.MouseButton
import java.time.LocalDate

class AccountEntryTableRow(
    private val actionHandler: (action: Action) -> Unit,
) : TableRow<FXAccountEntry>() {
    sealed class Action {
        object Add : Action()

        data class Edit(val entry: FXAccountEntry) : Action()

        data class Delete(val entry: FXAccountEntry) : Action()

        data class UpdateStatus(val status: TransactionStatus, val entry: FXAccountEntry) : Action()
    }

    companion object {
        private val futureDatePseudoClass: PseudoClass = PseudoClass.getPseudoClass("future-date")
    }

    private val editItem =
        MenuItem("Edit").apply {
            setOnAction { actionHandler.invoke(Action.Edit(item)) }
        }

    private val deleteItem =
        MenuItem("Delete").apply {
            setOnAction { actionHandler.invoke(Action.Delete(item)) }
        }

    private val transactionContextMenu =
        ContextMenu().apply {
            items.addAll(
                editItem,
                SeparatorMenuItem(),
                createStatusMenu(),
                SeparatorMenuItem(),
                deleteItem,
            )
        }

    init {
        setOnMouseClicked { event ->
            when {
                event.button == MouseButton.PRIMARY && event.clickCount == 2 ->
                    when (item) {
                        null -> {}
                        else -> actionHandler.invoke(Action.Edit(item))
                    }
            }
        }
    }

    override fun updateItem(
        item: FXAccountEntry?,
        empty: Boolean,
    ) {
        super.updateItem(item, empty)

        contextMenu = if (empty) null else transactionContextMenu

        val isFuture = !empty && item != null && item.dateProperty.value.isAfter(LocalDate.now())
        pseudoClassStateChanged(futureDatePseudoClass, isFuture)
    }

    private fun createStatusMenu() =
        Menu("Mark As").apply {
            val unreconciled = createStatusMenuItem(TransactionStatus.UNRECONCILED, "Unreconciled")
            val cleared = createStatusMenuItem(TransactionStatus.CLEARED, "Cleared (C)")
            val reconciled = createStatusMenuItem(TransactionStatus.RECONCILED, "Reconciled (R)")

            items.addAll(unreconciled, cleared, reconciled)
        }

    private fun createStatusMenuItem(
        status: TransactionStatus,
        text: String,
    ) = // TODO convert to RadioMenuItem and bind the selected property to the item's status
        MenuItem(text).apply {
            setOnAction { actionHandler.invoke(Action.UpdateStatus(status, item)) }
        }
}
