package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.textColumn
import com.munzenberger.money.presentation.transactions.TransactionEntryInput
import javafx.scene.control.TableView

/**
 * The split dialog's table of the transaction's entries, one row per entry in the form's order, showing each entry's
 * category, memo and amount as the user has entered them.
 *
 * The columns stretch to fill the table's width. Resizing a column takes the space from the last column only, leaving
 * the others as they are.
 */
internal class TransactionSplitTable : TableView<TransactionEntryInput>() {

    private val categoryColumn = textColumn<TransactionEntryInput>(
        id = "category",
        titleKey = "split_category_column_title",
    ) { it.categoryText }

    private val memoColumn = textColumn<TransactionEntryInput>(
        id = "memo",
        titleKey = "split_memo_column_title",
    ) { it.memo }

    // Shows the amount's text as entered, right-aligned like the other tables' amounts.
    private val amountColumn = textColumn<TransactionEntryInput>(
        id = "amount",
        titleKey = "split_amount_column_title",
    ) { it.amountText }.apply {
        styleClass += "amount-column"
    }

    init {
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        columns.setAll(categoryColumn, memoColumn, amountColumn)
        // The rows follow the form's order, so they aren't sortable.
        columns.forEach { it.isSortable = false }
    }

    /** Shows [entries], replacing the ones shown. Showing the same entries again leaves the rows as they are. */
    fun show(entries: List<TransactionEntryInput>) {
        if (entries != items) {
            items.setAll(entries)
        }
    }
}
