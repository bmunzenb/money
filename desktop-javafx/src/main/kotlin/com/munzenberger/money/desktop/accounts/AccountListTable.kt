package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.desktop.components.columnLayoutKey
import com.munzenberger.money.desktop.components.saveColumnLayout
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.accounts.maskAccountNumber
import com.munzenberger.money.presentation.preferences.UserPreferences
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.collections.FXCollections
import javafx.css.PseudoClass
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.util.Callback
import java.util.IdentityHashMap

/**
 * The account list's table. Each group's rows follow one another: the group's row, then its accounts' rows, then its
 * subtotal's row. Opening an account, by its name's link, a double click or Enter, calls [onAccountClick]; its row's
 * context menu calls [onAccountSettingsClick].
 *
 * The columns stretch to fill the table's width. They start at the same width, so they share it equally. Resizing a
 * column takes the space from the last column only, leaving the others as they are. Their order and widths are saved in
 * [preferences].
 */
class AccountListTable(
    preferences: UserPreferences,
    private val onAccountClick: (Account) -> Unit,
    private val onAccountSettingsClick: (Account) -> Unit,
) : TableView<AccountListRow>() {

    private val nameColumn = column(
        id = "name",
        titleKey = "account_name_column_title",
        groupValue = { it.title() },
        accountValue = { it.name }
    ).apply { setCellFactory { AccountNameCell(onAccountClick) } }

    private val numberColumn = column(
        id = "number",
        titleKey = "account_number_column_title",
        accountValue = { maskAccountNumber(it.number) }
    )

    private val balanceColumn = column(
        id = "balance",
        titleKey = "balance_column_title",
        subtotalValue = { strings.getString("subtotal_label") }
    ).apply {
        styleClass += "amount-column"
    }

    // The groups last shown, so showing the same groups again doesn't rebuild the rows.
    private var shownGroups: List<AccountGroup>? = null

    init {
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        columns.setAll(nameColumn, numberColumn, balanceColumn)
        setRowFactory { AccountListTableRow() }
        // Opens the selected account on Enter, as a double click does.
        setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                (selectionModel.selectedItem as? AccountListRow.AccountRow)?.let {
                    onAccountClick(it.account)
                    event.consume()
                }
            }
        }
        saveColumnLayout(preferences, COLUMN_LAYOUT_KEY)
        // Sorts the rows by the sorted columns, keeping each group's rows together. With no sorted columns, the rows
        // keep their order, as with the default sort policy.
        sortPolicy = Callback { table ->
            table.comparator?.let { comparator ->
                val sorted = table.items.sortedBySections(comparator)

                // Sorts the items in place, rather than replacing them, so the table keeps its selection, as with the
                // default sort policy.
                val positions = IdentityHashMap<AccountListRow, Int>()
                sorted.forEachIndexed { index, row -> positions[row] = index }
                FXCollections.sort(table.items, compareBy { positions.getValue(it) })
            }
            true
        }
    }

    /** Shows [groups]' rows, sorted by the sorted columns, unless they're the groups already shown. */
    fun setGroups(groups: List<AccountGroup>) {
        if (groups != shownGroups) {
            shownGroups = groups
            items.setAll(groups.flatMap { it.rows() })
            // The sort policy doesn't sort new rows by itself.
            sort()
        }
    }

    private fun column(
        id: String,
        titleKey: String,
        groupValue: (AccountGroup) -> String = { "" },
        subtotalValue: (AccountGroup) -> String = { "" },
        accountValue: (Account) -> String = { "" },
    ) = TableColumn<AccountListRow, String>(strings.getString(titleKey)).apply {
        this.id = id
        setCellValueFactory { features ->
            val value = when (val row = features.value) {
                is AccountListRow.GroupRow -> groupValue(row.group)
                is AccountListRow.AccountRow -> accountValue(row.account)
                is AccountListRow.SubtotalRow -> subtotalValue(row.group)
                null -> ""
            }
            ReadOnlyStringWrapper(value)
        }
    }

    // Marks the groups' and subtotals' rows so the stylesheet can set them apart from the accounts' rows, and the
    // closed accounts' rows so it can dim them. Gives the accounts' rows a context menu and opens their account on a
    // double click.
    private inner class AccountListTableRow : TableRow<AccountListRow>() {
        // Edits the account in the row. The row reuses it for whichever account it shows.
        private val accountContextMenu = ContextMenu(
            MenuItem(strings.getString("account_settings_menu_item_title")).apply {
                setOnAction {
                    (item as? AccountListRow.AccountRow)?.let { onAccountSettingsClick(it.account) }
                }
            }
        )

        init {
            setOnMouseClicked { event ->
                if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                    (item as? AccountListRow.AccountRow)?.let { onAccountClick(it.account) }
                }
            }
        }

        override fun updateItem(item: AccountListRow?, empty: Boolean) {
            super.updateItem(item, empty)
            pseudoClassStateChanged(GROUP_PSEUDO_CLASS, item is AccountListRow.GroupRow)
            pseudoClassStateChanged(SUBTOTAL_PSEUDO_CLASS, item is AccountListRow.SubtotalRow)
            pseudoClassStateChanged(
                CLOSED_PSEUDO_CLASS,
                (item as? AccountListRow.AccountRow)?.account?.isClosed == true,
            )
            contextMenu = if (item is AccountListRow.AccountRow) accountContextMenu else null
        }
    }

    private companion object {
        val COLUMN_LAYOUT_KEY = columnLayoutKey("account.list.columns")
        val GROUP_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("group")
        val SUBTOTAL_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("subtotal")
        val CLOSED_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("closed")
    }
}
