package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.setError
import com.munzenberger.money.desktop.components.setTextIfChanged
import com.munzenberger.money.presentation.transactions.TransactionEntryInput
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Control
import javafx.scene.control.TableCell
import javafx.scene.control.TextInputControl
import kotlin.uuid.Uuid

/**
 * Edits part of the entry in the cell's row in [field], whose text is typed in [editor]: the field itself, or a combo
 * box's editor. The field shows the entry's [textOf], and each change the user makes to it is passed to [onTextChange]
 * with the entry's key. Leaving the field is passed to [onFocusLost], and the field is outlined as an error while
 * [isError] is true of the entry.
 *
 * The table reuses a cell for another row as it scrolls or its rows are replaced. If that happens while the cell's
 * field has the focus, the user has in effect left the entry they were typing in, so that entry is passed to
 * [onFocusLost] before the cell shows the next one.
 */
internal class TransactionSplitFieldCell(
    private val field: Control,
    private val editor: TextInputControl,
    private val textOf: (TransactionEntryInput) -> String,
    private val onTextChange: (key: Uuid, text: String) -> Unit,
    private val onFocusLost: (key: Uuid) -> Unit = {},
    private val isError: (TransactionEntryInput) -> Boolean = { false },
) : TableCell<TransactionSplitRow, TransactionEntryInput>() {

    // Set while the cell fills in its row's text, so showing an entry, or another entry as the cell is reused, isn't
    // taken as the user changing it.
    private var isUpdating = false

    init {
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
        field.maxWidth = Double.MAX_VALUE
        editor.textProperty().addListener { _, _, text ->
            if (!isUpdating) item?.let { onTextChange(it.key, text) }
        }
        field.focusedProperty().addListener { _, _, focused ->
            if (!focused) item?.let { onFocusLost(it.key) }
        }
    }

    override fun updateItem(item: TransactionEntryInput?, empty: Boolean) {
        val previousKey = this.item?.key
        if (field.isFocused && previousKey != null && previousKey != item?.key) onFocusLost(previousKey)

        super.updateItem(item, empty)
        text = null
        if (empty || item == null) {
            graphic = null
        } else {
            isUpdating = true
            try {
                editor.setTextIfChanged(textOf(item))
            } finally {
                isUpdating = false
            }
            field.setError(isError(item))
            graphic = field
        }
    }
}
