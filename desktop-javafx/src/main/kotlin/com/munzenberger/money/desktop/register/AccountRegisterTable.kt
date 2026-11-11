package com.munzenberger.money.desktop.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.desktop.components.columnLayoutKey
import com.munzenberger.money.desktop.components.saveColumnLayout
import com.munzenberger.money.desktop.components.textColumn
import com.munzenberger.money.desktop.getString
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.preferences.UserPreferences
import com.munzenberger.money.presentation.register.RegisterAmountColumns
import com.munzenberger.money.presentation.register.decrease
import com.munzenberger.money.presentation.register.increase
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.css.PseudoClass
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.MouseButton
import kotlinx.datetime.toJavaLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * An account register's table of entries, in the register's order. The columns can't be sorted, since each entry's
 * balance is the account's balance after it in that order. Entries dated after today are dimmed.
 *
 * Each entry's amount shows as a positive value in one of two columns, for amounts that lower the account's balance
 * and amounts that raise it, named for the kind of account (e.g. Payment and Deposit, or Charge and Payment). Only the
 * balance column shows signs, with a negative balance in red.
 *
 * The columns stretch to fill the table's width. They start at the same width, so they share it equally. Resizing a
 * column takes the space from the last column only, leaving the others as they are. Their order and widths are saved in
 * [preferences].
 *
 * Double-clicking an entry's row, or choosing See Details from its context menu, calls [onEntryClick] with the entry.
 */
class AccountRegisterTable(
    preferences: UserPreferences,
    private val onEntryClick: (RegisterEntry) -> Unit,
) : TableView<RegisterEntry>() {

    private val numberColumn = column(id = "number", titleKey = "register_number_column_title") {
        it.number.orEmpty()
    }

    private val dateColumn = column(id = "date", titleKey = "register_date_column_title") {
        DATE_FORMATTER.format(it.date.toJavaLocalDate())
    }

    private val payeeColumn = column(id = "payee", titleKey = "register_payee_column_title") {
        it.payeeName.orEmpty()
    }

    private val categoryColumn = column(id = "category", titleKey = "register_category_column_title") {
        it.category().orEmpty()
    }

    private val statusColumn = column(id = "status", titleKey = "register_status_column_title") {
        when (it.status.value) {
            TransactionStatusConstant.Unreconciled -> ""
            TransactionStatusConstant.Cleared -> strings.getString("register_status_cleared")
            TransactionStatusConstant.Reconciled -> strings.getString("register_status_reconciled")
        }
    }.apply {
        styleClass += "status-column"
    }

    // Each entry's amount shows, as a positive value, in the column for whether it lowers or raises the balance. Their
    // titles depend on the kind of account, so show sets them.
    private val decreaseColumn = amountColumn(id = DECREASE_COLUMN_ID) { it.decrease }

    private val increaseColumn = amountColumn(id = INCREASE_COLUMN_ID) { it.increase }

    private val balanceColumn = amountColumn(id = "balance", strings.getString("balance_column_title")) { it.balance }

    init {
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        columns.setAll(
            numberColumn,
            dateColumn,
            payeeColumn,
            categoryColumn,
            statusColumn,
            decreaseColumn,
            increaseColumn,
            balanceColumn,
        )
        saveColumnLayout(preferences, COLUMN_LAYOUT_KEY)
        setRowFactory { AccountRegisterTableRow() }
        showAmountColumnTitles(RegisterAmountColumns.Other)
    }

    /** Shows [entries], replacing the ones shown, with amount columns named for [amountColumns]. */
    fun show(entries: List<RegisterEntry>, amountColumns: RegisterAmountColumns) {
        if (entries != items) items.setAll(entries)
        showAmountColumnTitles(amountColumns)
    }

    private fun showAmountColumnTitles(amountColumns: RegisterAmountColumns) {
        val (decreaseKey, increaseKey) = when (amountColumns) {
            RegisterAmountColumns.Banking ->
                "register_banking_decrease_column_title" to "register_banking_increase_column_title"
            RegisterAmountColumns.CreditCard ->
                "register_credit_card_decrease_column_title" to "register_credit_card_increase_column_title"
            RegisterAmountColumns.Loan ->
                "register_loan_decrease_column_title" to "register_loan_increase_column_title"
            RegisterAmountColumns.Other ->
                "register_other_decrease_column_title" to "register_other_increase_column_title"
        }
        decreaseColumn.text = strings.getString(decreaseKey)
        increaseColumn.text = strings.getString(increaseKey)
    }

    private fun column(id: String, titleKey: String, value: (RegisterEntry) -> String) =
        textColumn(id, titleKey, value).apply { isSortable = false }

    // A right-aligned column of amounts, with negative amounts in red. Only the balance column has any, since the
    // others show their amounts as positive values.
    private fun amountColumn(id: String, title: String = "", value: (RegisterEntry) -> Money?) =
        TableColumn<RegisterEntry, Money?>(title).apply {
            this.id = id
            isSortable = false
            styleClass += "amount-column"
            setCellValueFactory { features -> ReadOnlyObjectWrapper(features.value?.let(value)) }
            setCellFactory { RegisterAmountCell() }
        }

    // Marks the rows of entries dated after today so the stylesheet can dim them. Today is checked as each row is
    // shown, so a row shown before midnight stays as it is until it's shown again. Gives the entries' rows a context
    // menu and shows their entry's details on a double click.
    private inner class AccountRegisterTableRow : TableRow<RegisterEntry>() {
        // Shows the details of the entry in the row. The row reuses it for whichever entry it shows.
        private val entryContextMenu = ContextMenu(
            MenuItem(strings.getString("register_entry_menu_item_title")).apply {
                styleClass += "default-menu-item"
                setOnAction { item?.let(onEntryClick) }
            },
        )

        init {
            setOnMouseClicked { event ->
                if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                    item?.let(onEntryClick)
                }
            }
        }

        override fun updateItem(item: RegisterEntry?, empty: Boolean) {
            super.updateItem(item, empty)
            pseudoClassStateChanged(
                FUTURE_PSEUDO_CLASS,
                item != null && item.date.toJavaLocalDate().isAfter(LocalDate.now()),
            )
            contextMenu = if (item != null && !empty) entryContextMenu else null
        }
    }

    private companion object {
        val FUTURE_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("future")
        val COLUMN_LAYOUT_KEY = columnLayoutKey("account.register.columns")
        const val DECREASE_COLUMN_ID = "decrease"
        const val INCREASE_COLUMN_ID = "increase"

        // The id of the single amount column the decrease and increase columns replaced.
        const val AMOUNT_COLUMN_ID = "amount"

        // The locale's numeric date, with a four-digit year, e.g. 1/15/2026 in the US.
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedPattern("yMd")
    }
}

/** The entry's category, or the account it transfers to, or null if it has neither. */
private fun RegisterEntry.category(): String? = when (this) {
    is RegisterEntry.Transaction -> when (entries.size) {
        0 -> null
        1 -> entries.single().title()
        else -> strings.getString("register_split_value")
    }
    is RegisterEntry.Transfer -> strings.getString("register_transfer_value", accountName)
}

private fun RegisterEntry.Transaction.Entry.title(): String = when (this) {
    is RegisterEntry.Transaction.Entry.Category -> parentCategoryName
        ?.let { strings.getString("register_subcategory_value", it, categoryName) }
        ?: categoryName
    is RegisterEntry.Transaction.Entry.Transfer -> strings.getString("register_transfer_value", accountName)
}
