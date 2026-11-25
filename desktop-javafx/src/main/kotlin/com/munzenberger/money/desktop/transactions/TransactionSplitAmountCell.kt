package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.setError
import com.munzenberger.money.desktop.components.setTextIfChanged
import com.munzenberger.money.presentation.transactions.TransactionEntryInput
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TableCell
import javafx.scene.control.TextField
import kotlin.uuid.Uuid

/**
 * Shows an entry's amount in a right-aligned text field, as the transaction form's amount field does, so the user can
 * change it. Each change to the text is passed to [onAmountChange] with the key of the entry in the cell's row, and
 * leaving the field is passed to [onAmountFocusLost], so the amount is checked and reformatted. An amount that isn't
 * valid is outlined as an error.
 */
internal class TransactionSplitAmountCell(
    private val onAmountChange: (key: Uuid, amountText: String) -> Unit,
    private val onAmountFocusLost: (key: Uuid) -> Unit,
) : TableCell<TransactionSplitRow, TransactionEntryInput>() {

    // Set while the cell fills in its row's text, so showing an entry, or another entry as the cell is reused, isn't
    // taken as the user changing it.
    private var isUpdating = false

    private val textField = TextField().apply {
        alignment = Pos.CENTER_RIGHT
        maxWidth = Double.MAX_VALUE
        textProperty().addListener { _, _, amountText ->
            if (!isUpdating) {
                item?.let { onAmountChange(it.key, amountText) }
            }
        }
        focusedProperty().addListener { _, _, focused ->
            if (!focused) item?.let { onAmountFocusLost(it.key) }
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
                textField.setTextIfChanged(item.amountText)
            } finally {
                isUpdating = false
            }
            textField.setError(item.isAmountError)
            graphic = textField
        }
    }
}
