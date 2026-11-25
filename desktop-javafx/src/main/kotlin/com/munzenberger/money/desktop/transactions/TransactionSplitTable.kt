package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.AutoCompleteComboBox
import com.munzenberger.money.desktop.components.column
import com.munzenberger.money.presentation.transactions.TransactionEntryInput
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import kotlin.uuid.Uuid

/**
 * The split dialog's table of the transaction's entries, one row per entry in the form's order, showing each entry's
 * category, memo and amount as the user has entered them. Each row's category is edited in a combo box, as in the
 * transaction form, and each change to its text is passed to [onCategoryChange] with the entry's key. Each row's memo
 * is edited in a text field, and each change to it is passed to [onMemoChange] with the entry's key. Each row's amount
 * is edited in a text field, as in the transaction form: each change to its text is passed to [onAmountChange], and
 * leaving it to [onAmountFocusLost], with the entry's key.
 *
 * Each row holds its entry in a property, so a change to an entry, such as the user typing in one of its fields,
 * updates the row's cells in place. Replacing a row in the table's items would instead have the table rebuild all its
 * cells, taking the focus from the field being typed in.
 *
 * The columns stretch to fill the table's width. Resizing a column takes the space from the last column only, leaving
 * the others as they are.
 */
internal class TransactionSplitTable(
    onCategoryChange: (key: Uuid, categoryText: String) -> Unit,
    onMemoChange: (key: Uuid, memo: String) -> Unit,
    onAmountChange: (key: Uuid, amountText: String) -> Unit,
    onAmountFocusLost: (key: Uuid) -> Unit,
) : TableView<TransactionSplitRow>() {

    // Shared by the rows' combo boxes, so they all list the same categories.
    private val categoryTexts: ObservableList<String> = FXCollections.observableArrayList()

    // Picks a category or transfer account from the list, or takes a new category's typed name, as the form's does.
    private val categoryColumn = entryColumn(id = "category", titleKey = "split_category_column_title").apply {
        setCellFactory {
            val comboBox = AutoCompleteComboBox<String>().apply { items = categoryTexts }
            TransactionSplitFieldCell(
                field = comboBox,
                editor = comboBox.editor,
                textOf = { it.categoryText },
                onTextChange = onCategoryChange,
            )
        }
    }

    private val memoColumn = entryColumn(id = "memo", titleKey = "split_memo_column_title").apply {
        setCellFactory {
            val textField = TextField()
            TransactionSplitFieldCell(
                field = textField,
                editor = textField,
                textOf = { it.memo },
                onTextChange = onMemoChange,
            )
        }
    }

    // Edits the amount's text as entered, right-aligned like the other tables' amounts. Leaving it checks the amount,
    // as the form's amount field does, and an amount that isn't valid is outlined as an error.
    private val amountColumn = entryColumn(id = "amount", titleKey = "split_amount_column_title").apply {
        styleClass += "amount-column"
        setCellFactory {
            val textField = TextField().apply { alignment = Pos.CENTER_RIGHT }
            TransactionSplitFieldCell(
                field = textField,
                editor = textField,
                textOf = { it.amountText },
                onTextChange = onAmountChange,
                onFocusLost = onAmountFocusLost,
                isError = { it.isAmountError },
            )
        }
    }

    init {
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        columns.setAll(categoryColumn, memoColumn, amountColumn)
        // The rows follow the form's order, so they aren't sortable.
        columns.forEach { it.isSortable = false }
    }

    /**
     * Shows [entries], replacing the ones shown. While the rows are for the same entries in the same order, each row's
     * entry is updated in place, so a field the user is typing in keeps its focus. Otherwise the rows are replaced.
     */
    fun show(entries: List<TransactionEntryInput>) {
        val isSameRows = entries.size == items.size && entries.indices.all { entries[it].key == items[it].key }
        if (isSameRows) {
            // Leaves the rows whose entries are unchanged, so only the changed rows' cells update.
            entries.forEachIndexed { index, entry ->
                val row = items[index]
                if (row.entry.value != entry) row.entry.value = entry
            }
        } else {
            items.setAll(entries.map(::TransactionSplitRow))
        }
    }

    /** Lists [texts] in the category combo boxes, as the categories' and transfer accounts' text. */
    fun showCategories(texts: List<String>) {
        if (texts != categoryTexts) {
            categoryTexts.setAll(texts)
        }
    }

    // Each column's cells are given the row's whole entry and show the part of it they edit.
    private fun entryColumn(id: String, titleKey: String) =
        column<TransactionSplitRow, TransactionEntryInput>(id, titleKey) { it.entry }
}

/** A row of the split dialog's table, holding the entry with [key] as it changes. */
internal class TransactionSplitRow(entry: TransactionEntryInput) {
    val key: Uuid = entry.key
    val entry: ObjectProperty<TransactionEntryInput> = SimpleObjectProperty(entry)
}
