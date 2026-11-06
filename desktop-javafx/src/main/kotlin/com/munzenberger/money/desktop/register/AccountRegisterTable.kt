package com.munzenberger.money.desktop.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.desktop.components.columnLayoutKey
import com.munzenberger.money.desktop.components.saveColumnLayout
import com.munzenberger.money.desktop.getString
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.preferences.UserPreferences
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * An account register's table of entries, in the register's order. The columns can't be sorted, since each entry's
 * balance is the account's balance after it in that order.
 *
 * The columns stretch to fill the table's width. They start at the same width, so they share it equally. Resizing a
 * column takes the space from the last column only, leaving the others as they are. Their order and widths are saved in
 * [preferences].
 */
class AccountRegisterTable(preferences: UserPreferences) : TableView<RegisterEntry>() {

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

    private val amountColumn = amountColumn(id = "amount", titleKey = "amount_column_title") { it.amount }

    private val balanceColumn = amountColumn(id = "balance", titleKey = "balance_column_title") { it.balance }

    init {
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        columns.setAll(numberColumn, dateColumn, payeeColumn, categoryColumn, statusColumn, amountColumn, balanceColumn)
        saveColumnLayout(preferences, COLUMN_LAYOUT_KEY)
    }

    /** Shows [entries], replacing the ones shown. */
    fun show(entries: List<RegisterEntry>) {
        if (entries != items) items.setAll(entries)
    }

    private fun column(id: String, titleKey: String, value: (RegisterEntry) -> String) =
        TableColumn<RegisterEntry, String>(strings.getString(titleKey)).apply {
            this.id = id
            isSortable = false
            setCellValueFactory { features -> ReadOnlyStringWrapper(features.value?.let(value).orEmpty()) }
        }

    // A right-aligned column of amounts, with negative amounts in red.
    private fun amountColumn(id: String, titleKey: String, value: (RegisterEntry) -> Money) =
        TableColumn<RegisterEntry, Money?>(strings.getString(titleKey)).apply {
            this.id = id
            isSortable = false
            styleClass += "amount-column"
            setCellValueFactory { features -> ReadOnlyObjectWrapper(features.value?.let(value)) }
            setCellFactory { RegisterAmountCell() }
        }

    private companion object {
        val COLUMN_LAYOUT_KEY = columnLayoutKey("account.register.columns")
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
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
