package com.munzenberger.money.desktop.register

import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.desktop.components.columnLayoutKey
import com.munzenberger.money.desktop.components.saveColumnLayout
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.preferences.UserPreferences
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView

/**
 * An account register's table of entries.
 *
 * The columns stretch to fill the table's width. They start at the same width, so they share it equally. Resizing a
 * column takes the space from the last column only, leaving the others as they are. Their order and widths are saved in
 * [preferences].
 */
class AccountRegisterTable(preferences: UserPreferences) : TableView<RegisterEntry>() {

    private val numberColumn = column(id = "number", titleKey = "register_number_column_title")

    private val dateColumn = column(id = "date", titleKey = "register_date_column_title")

    private val detailsColumn = column(id = "details", titleKey = "register_details_column_title")

    private val statusColumn = column(id = "status", titleKey = "register_status_column_title")

    private val amountColumn = column(id = "amount", titleKey = "amount_column_title").apply {
        styleClass += "amount-column"
    }

    private val balanceColumn = column(id = "balance", titleKey = "balance_column_title").apply {
        styleClass += "amount-column"
    }

    init {
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        columns.setAll(numberColumn, dateColumn, detailsColumn, statusColumn, amountColumn, balanceColumn)
        saveColumnLayout(preferences, COLUMN_LAYOUT_KEY)
    }

    private fun column(id: String, titleKey: String) =
        TableColumn<RegisterEntry, String>(strings.getString(titleKey)).apply {
            this.id = id
        }

    private companion object {
        val COLUMN_LAYOUT_KEY = columnLayoutKey("account.register.columns")
    }
}
