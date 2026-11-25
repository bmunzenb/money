package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.textColumn
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.transactions.TransactionEntryInput
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import kotlin.uuid.Uuid

/**
 * The split dialog's table of the transaction's entries, one row per entry in the form's order, showing each entry's
 * category, memo and amount as the user has entered them. Each row's category is edited in a combo box, as in the
 * transaction form, and each change to its text is passed to [onCategoryChange] with the entry's key. Each row's memo
 * is edited in a text field, and each change to it is passed to [onMemoChange] with the entry's key. Each row's amount
 * is edited in a text field, as in the transaction form: each change to its text is passed to [onAmountChange], and
 * leaving it to [onAmountFocusLost], with the entry's key.
 *
 * The columns stretch to fill the table's width. Resizing a column takes the space from the last column only, leaving
 * the others as they are.
 */
class TransactionSplitTable(
    onCategoryChange: (key: Uuid, categoryText: String) -> Unit,
    onMemoChange: (key: Uuid, memo: String) -> Unit,
    onAmountChange: (key: Uuid, amountText: String) -> Unit,
    onAmountFocusLost: (key: Uuid) -> Unit,
) : TableView<TransactionEntryInput>() {

    // Shared by the rows' combo boxes, so they all list the same categories.
    private val categoryTexts: ObservableList<String> = FXCollections.observableArrayList()

    private val categoryColumn = textColumn<TransactionEntryInput>(
        id = "category",
        titleKey = "split_category_column_title",
    ) { it.categoryText }.apply {
        setCellFactory { TransactionSplitCategoryCell(categoryTexts, onCategoryChange) }
    }

    private val memoColumn = textColumn<TransactionEntryInput>(
        id = "memo",
        titleKey = "split_memo_column_title",
    ) { it.memo }.apply {
        setCellFactory { TransactionSplitMemoCell(onMemoChange) }
    }

    // Edits the amount's text as entered, right-aligned like the other tables' amounts. Its value is the whole entry,
    // so the cell also shows the amount's error.
    private val amountColumn = TableColumn<TransactionEntryInput, TransactionEntryInput>(
        strings.getString("split_amount_column_title"),
    ).apply {
        id = "amount"
        styleClass += "amount-column"
        setCellValueFactory { features -> ReadOnlyObjectWrapper(features.value) }
        setCellFactory { TransactionSplitAmountCell(onAmountChange, onAmountFocusLost) }
    }

    init {
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        columns.setAll(categoryColumn, memoColumn, amountColumn)
        // The rows follow the form's order, so they aren't sortable.
        columns.forEach { it.isSortable = false }
    }

    /**
     * Shows [entries], replacing the ones shown. Showing the same entries again leaves the rows as they are, and only
     * the rows that changed are replaced, so a field the user is typing in keeps its focus.
     */
    fun show(entries: List<TransactionEntryInput>) {
        if (entries.size == items.size) {
            entries.forEachIndexed { index, entry -> if (items[index] != entry) items[index] = entry }
        } else {
            items.setAll(entries)
        }
    }

    /** Lists [texts] in the category combo boxes, as the categories' and transfer accounts' text. */
    fun showCategories(texts: List<String>) {
        if (texts != categoryTexts) {
            categoryTexts.setAll(texts)
        }
    }
}
