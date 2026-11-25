package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.setTextIfChanged
import com.munzenberger.money.presentation.transactions.TransactionEntryInput
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TableCell
import javafx.scene.control.TextField
import kotlin.uuid.Uuid

/**
 * Shows an entry's memo in a text field, so the user can change it. Each change to the text is passed to
 * [onMemoChange] with the key of the entry in the cell's row.
 */
internal class TransactionSplitMemoCell(
    private val onMemoChange: (key: Uuid, memo: String) -> Unit,
) : TableCell<TransactionSplitRow, TransactionEntryInput>() {

    // Set while the cell fills in its row's text, so showing an entry, or another entry as the cell is reused, isn't
    // taken as the user changing it.
    private var isUpdating = false

    private val textField = TextField().apply {
        maxWidth = Double.MAX_VALUE
        textProperty().addListener { _, _, memo ->
            if (!isUpdating) {
                item?.let { onMemoChange(it.key, memo) }
            }
        }
    }

    init {
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
    }

    override fun updateItem(item: TransactionEntryInput?, empty: Boolean) {
        super.updateItem(item, empty)
        text = null
        if (empty || item == null) {
            graphic = null
        } else {
            isUpdating = true
            try {
                textField.setTextIfChanged(item.memo)
            } finally {
                isUpdating = false
            }
            graphic = textField
        }
    }
}
