package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.AutoCompleteComboBox
import com.munzenberger.money.desktop.components.setTextIfChanged
import com.munzenberger.money.presentation.transactions.TransactionEntryInput
import javafx.collections.ObservableList
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TableCell
import kotlin.uuid.Uuid

/**
 * Shows an entry's category text in a combo box, as the transaction form's category field does, so the user can pick
 * a category or transfer account from [categoryTexts] or type a new category's name. Each change to the text is passed
 * to [onCategoryChange] with the key of the entry in the cell's row.
 */
internal class TransactionSplitCategoryCell(
    categoryTexts: ObservableList<String>,
    private val onCategoryChange: (key: Uuid, categoryText: String) -> Unit,
) : TableCell<TransactionEntryInput, String>() {

    // Set while the cell fills in its row's text, so showing an entry, or another entry as the cell is reused, isn't
    // taken as the user changing it.
    private var isUpdating = false

    private val comboBox = AutoCompleteComboBox<String>().apply {
        items = categoryTexts
        maxWidth = Double.MAX_VALUE
        editor.textProperty().addListener { _, _, categoryText ->
            if (!isUpdating) {
                tableRow?.item?.let { onCategoryChange(it.key, categoryText) }
            }
        }
    }

    init {
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        text = null
        if (empty) {
            graphic = null
        } else {
            isUpdating = true
            try {
                comboBox.editor.setTextIfChanged(item.orEmpty())
            } finally {
                isUpdating = false
            }
            graphic = comboBox
        }
    }
}
