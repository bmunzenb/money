package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.desktop.components.ScreenHeading
import com.munzenberger.money.desktop.components.columnLayoutKey
import com.munzenberger.money.desktop.components.saveColumnLayout
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.accounts.AccountListUiState
import com.munzenberger.money.presentation.accounts.AccountListViewModel
import com.munzenberger.money.presentation.accounts.LoadState
import com.munzenberger.money.presentation.accounts.loadedOrEmpty
import com.munzenberger.money.presentation.accounts.maskAccountNumber
import com.munzenberger.money.presentation.preferences.UserPreferences
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.collections.FXCollections
import javafx.css.PseudoClass
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Callback
import javafx.util.StringConverter
import java.util.IdentityHashMap
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Lists the accounts, with a heading and a row of controls to group them and add a new one. */
class AccountListScreen(scope: ScreenScope) : VBox(Spacing.CONTROL_SPACING), KoinComponent {
    private val viewModel: AccountListViewModel = scope.viewModel()

    private val preferences: UserPreferences by inject()

    private val heading = ScreenHeading(strings.getString("account_list_title"))

    // Choosing the grouping the state already has, as render does, leaves the ViewModel's grouping unchanged. A new
    // grouping clears the table's selection, since the selected row moves to another group or goes away.
    private val groupingComboBox = ComboBox<AccountGrouping>().apply {
        items.setAll(AccountGrouping.entries)
        converter = AccountGroupingConverter
        setOnAction {
            accountTable.selectionModel.clearSelection()
            value?.let(viewModel::onGroupingChange)
        }
    }

    private val groupingLabel = Label(strings.getString("account_grouping_label")).apply {
        labelFor = groupingComboBox
    }

    private val addAccountButton = Button(strings.getString("add_account_button_title")).apply {
        setOnAction { viewModel.onAddAccountClick() }
    }

    // Pushes the add account button to the end of the row.
    private val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }

    private val controls = HBox(Spacing.CONTROL_SPACING).apply {
        alignment = Pos.CENTER_LEFT
        children.setAll(groupingLabel, groupingComboBox, spacer, addAccountButton)
    }

    private val nameColumn = column(
        id = "name",
        titleKey = "account_name_column_title",
        groupValue = { it.title() },
        accountValue = { it.name }
    ).apply { setCellFactory { AccountNameCell(viewModel::onAccountClick) } }

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
        style = "-fx-alignment: CENTER-RIGHT;"
    }

    // The table's placeholder while the groups load.
    private val loadingProgress = ProgressIndicator()

    // The table's placeholder when the groups fail to load.
    private val loadErrorMessage = Label(strings.getString("account_list_load_error_message")).apply {
        styleClass += "error-message"
        isWrapText = true
    }

    // The table's placeholder when the groups have loaded with no accounts.
    // It never shows as visited, so it looks the same each time the table is empty.
    private val emptyMessage = Hyperlink(strings.getString("account_list_empty_message")).apply {
        setOnAction {
            isVisited = false
            viewModel.onAddAccountClick()
        }
    }

    // The columns stretch to fill the table's width. They start at the same width, so they share it equally.
    // Resizing a column takes the space from the last column only, leaving the others as they are.
    // Each group's rows follow one another: the group's row, then its accounts' rows, then its subtotal's row.
    private val accountTable = TableView<AccountListRow>().apply {
        setVgrow(this, Priority.ALWAYS)
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        columns.setAll(nameColumn, numberColumn, balanceColumn)
        // Marks the groups' and subtotals' rows so the stylesheet can set them apart from the accounts' rows.
        setRowFactory {
            object : TableRow<AccountListRow>() {
                override fun updateItem(item: AccountListRow?, empty: Boolean) {
                    super.updateItem(item, empty)
                    pseudoClassStateChanged(GROUP_PSEUDO_CLASS, item is AccountListRow.GroupRow)
                    pseudoClassStateChanged(SUBTOTAL_PSEUDO_CLASS, item is AccountListRow.SubtotalRow)
                }
            }
        }
        saveColumnLayout(preferences, COLUMN_LAYOUT_KEY)
        // Sorts the groups, and the accounts in each group, by the sorted columns, keeping each group's row first and
        // its subtotal's row last. Account type and account class groups stay in the order of their constants instead.
        // With no sorted columns, the rows keep their order, as with the default sort policy.
        sortPolicy = Callback { table ->
            table.comparator?.let { comparator ->
                val sections = table.items.sections()
                val sectionOrder: Comparator<List<AccountListRow>> = when {
                    sections.all { it.first().accountType() != null } -> compareBy { it.first().accountType() }
                    sections.all { it.first().accountClass() != null } -> compareBy { it.first().accountClass() }
                    else -> compareBy(comparator) { it.first() }
                }
                val rowOrder = compareBy<AccountListRow> { it.sectionPosition() }.then(comparator)
                val sorted = sections.sortedWith(sectionOrder).flatMap { it.sortedWith(rowOrder) }

                // Sorts the items in place, rather than replacing them, so the table keeps its selection, as with the
                // default sort policy.
                val positions = IdentityHashMap<AccountListRow, Int>()
                sorted.forEachIndexed { index, row -> positions[row] = index }
                FXCollections.sort(table.items, compareBy { positions.getValue(it) })
            }
            true
        }
    }

    // Stays at the bottom of the screen, since the table above it takes the rest of the height. It shows only
    // the label until the total balance is available. Its labels inherit the bold font.
    private val totalBalance = HBox(Spacing.CONTROL_SPACING).apply {
        alignment = Pos.CENTER_RIGHT
        style = "-fx-font-weight: bold;"
        children.setAll(Label(strings.getString("total_balance_label")))
    }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(heading, controls, accountTable, totalBalance)
        scope.collect(viewModel.state, ::render)
    }

    // The groups last shown in the table, so a state that changes only the grouping doesn't rebuild the rows.
    private var renderedGroups: List<AccountGroup>? = null

    private fun render(state: AccountListUiState) {
        groupingComboBox.value = state.grouping

        accountTable.placeholder = when (state.groups) {
            LoadState.Loading -> loadingProgress
            LoadState.Error -> loadErrorMessage
            is LoadState.Loaded -> emptyMessage
        }

        totalBalance.isVisible = state.groups.loadedOrEmpty.isNotEmpty()

        val groups = state.groups.loadedOrEmpty
        if (groups != renderedGroups) {
            renderedGroups = groups
            accountTable.items.setAll(groups.flatMap { it.rows() })
            // The sort policy doesn't sort new rows by itself.
            accountTable.sort()
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

    /** Shows an account's name as a link, and a group's or subtotal's name as text. */
    private class AccountNameCell(
        private val onAccountClick: (Account) -> Unit,
    ) : TableCell<AccountListRow, String>() {
        // Opens the account in the cell's row. It never shows as visited, since the cell reuses it for other accounts.
        private val link = Hyperlink().apply {
            padding = Insets.EMPTY
            setOnAction {
                isVisited = false
                (tableRow?.item as? AccountListRow.AccountRow)?.let { onAccountClick(it.account) }
            }
        }

        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            if (!empty && tableRow?.item is AccountListRow.AccountRow) {
                link.text = item
                text = null
                graphic = link
            } else {
                text = if (empty) null else item
                graphic = null
            }
        }
    }

    private object AccountGroupingConverter : StringConverter<AccountGrouping>() {
        override fun toString(grouping: AccountGrouping?): String = grouping?.let {
            strings.getString(
                when (it) {
                    AccountGrouping.None -> "account_grouping_none"
                    AccountGrouping.AccountType -> "account_grouping_account_type"
                    AccountGrouping.AccountClass -> "account_grouping_account_class"
                    AccountGrouping.Bank -> "account_grouping_bank"
                }
            )
        }.orEmpty()

        // The combo box isn't editable, so it never converts text back to a grouping.
        override fun fromString(string: String?): AccountGrouping? = null
    }

    private companion object {
        val COLUMN_LAYOUT_KEY = columnLayoutKey("account.list.columns")
        val GROUP_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("group")
        val SUBTOTAL_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("subtotal")
    }
}

// A group's rows: its group row, then its accounts' rows, then its subtotal's row. The all accounts group has only its
// accounts' rows.
private fun AccountGroup.rows(): List<AccountListRow> {
    val accountRows = accounts.map(AccountListRow::AccountRow)
    return if (this is AccountGroup.All) {
        accountRows
    } else {
        listOf(AccountListRow.GroupRow(this)) + accountRows + AccountListRow.SubtotalRow(this)
    }
}

// Splits the rows into their groups' sections: each group's row, then its accounts' rows, then its subtotal's row. The
// all accounts group has no group or subtotal row, so its section is just its accounts' rows.
private fun List<AccountListRow>.sections(): List<List<AccountListRow>> {
    val sections = mutableListOf<MutableList<AccountListRow>>()
    forEach { row ->
        if (row is AccountListRow.GroupRow || sections.isEmpty()) {
            sections += mutableListOf<AccountListRow>()
        }
        sections.last() += row
    }
    return sections
}

// Where the row goes in its group's section: the group's row first, then its accounts' rows, then its subtotal's row.
private fun AccountListRow.sectionPosition(): Int = when (this) {
    is AccountListRow.GroupRow -> 0
    is AccountListRow.AccountRow -> 1
    is AccountListRow.SubtotalRow -> 2
}

// The account type of an account type group's row, or null for any other row.
private fun AccountListRow.accountType(): AccountTypeConstant? =
    ((this as? AccountListRow.GroupRow)?.group as? AccountGroup.ByAccountType)?.accountType?.value

// The account class of an account class group's row, or null for any other row.
private fun AccountListRow.accountClass(): AccountClassConstant? =
    ((this as? AccountListRow.GroupRow)?.group as? AccountGroup.ByAccountClass)?.accountClass?.value

private fun AccountGroup.title(): String = when (this) {
    is AccountGroup.All -> strings.getString("account_group_all_title")
    is AccountGroup.ByAccountType -> accountType.value.label
    is AccountGroup.ByAccountClass -> strings.getString(
        when (accountClass.value) {
            AccountClassConstant.Assets -> "account_class_assets"
            AccountClassConstant.Liabilities -> "account_class_liabilities"
        }
    )
    is AccountGroup.ByBank -> bank?.name ?: strings.getString("account_group_no_bank_title")
}
