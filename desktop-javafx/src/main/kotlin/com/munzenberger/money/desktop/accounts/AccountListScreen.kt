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
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableCell
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Callback
import javafx.util.StringConverter
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

    private val nameColumn = column("name", "account_name_column_title", groupValue = { it.title() }) { it.name }
        .apply { setCellFactory { AccountNameCell(viewModel::onAccountClick) } }

    private val numberColumn = column("number", "account_number_column_title") { maskAccountNumber(it.number) }

    // Empty until account balances are available, apart from the subtotals' labels. The column's style applies to its
    // cells, aligning their content to the right.
    private val balanceColumn = column(
        "balance",
        "balance_column_title",
        subtotalValue = { strings.getString("subtotal_label") },
    ) { "" }.apply {
        style = "-fx-alignment: CENTER-RIGHT;"
    }

    // The columns stretch to fill the table's width. They start at the same width, so they share it equally.
    // Resizing a column takes the space from the last column only, leaving the others as they are.
    // Each group is a top-level row under the hidden root, with its accounts' rows and then its subtotal's row under
    // it.
    private val accountTable = TreeTableView<AccountListRow>(TreeItem()).apply {
        setVgrow(this, Priority.ALWAYS)
        isShowRoot = false
        // Keeps the disclosure arrow beside the group's title when the user moves the columns.
        treeColumn = nameColumn
        columns.setAll(nameColumn, numberColumn, balanceColumn)
        columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        // Marks the groups' and subtotals' rows so the stylesheet can set them apart from the accounts' rows.
        setRowFactory {
            object : TreeTableRow<AccountListRow>() {
                override fun updateItem(item: AccountListRow?, empty: Boolean) {
                    super.updateItem(item, empty)
                    pseudoClassStateChanged(GROUP_PSEUDO_CLASS, item is AccountListRow.GroupRow)
                    pseudoClassStateChanged(SUBTOTAL_PSEUDO_CLASS, item is AccountListRow.SubtotalRow)
                }
            }
        }
        saveColumnLayout(preferences, COLUMN_LAYOUT_KEY)
        // Sorts the groups, and the accounts in each group, by the sorted columns, keeping each group's subtotal
        // last. Account type groups stay in the order of their constants instead. With no sorted columns, the rows
        // keep their order, as with the default sort policy.
        sortPolicy = Callback { table ->
            table.comparator?.let { comparator ->
                val subtotalLast = compareBy<TreeItem<AccountListRow>> { it.value is AccountListRow.SubtotalRow }
                    .then(comparator)
                val groupOrder = if (table.root.children.all { it.accountType() != null }) {
                    compareBy { it.accountType() }
                } else {
                    comparator
                }
                FXCollections.sort(table.root.children, groupOrder)
                table.root.children.forEach { FXCollections.sort(it.children, subtotalLast) }
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

        val groups = state.groups.loadedOrEmpty
        if (groups != renderedGroups) {
            renderedGroups = groups
            accountTable.root.children.setAll(groups.map(::groupItem))
            // The sort policy doesn't sort new rows by itself.
            accountTable.sort()
        }
    }

    private fun groupItem(group: AccountGroup) =
        TreeItem<AccountListRow>(AccountListRow.GroupRow(group)).apply {
            isExpanded = true
            children.setAll(
                group.accounts.map { TreeItem<AccountListRow>(AccountListRow.AccountRow(it)) } +
                    TreeItem(AccountListRow.SubtotalRow(group))
            )
        }

    // The id identifies the column's saved layout. A group's and a subtotal's rows are empty in the columns without
    // a group or subtotal value.
    private fun column(
        id: String,
        titleKey: String,
        groupValue: (AccountGroup) -> String = { "" },
        subtotalValue: (AccountGroup) -> String = { "" },
        accountValue: (Account) -> String,
    ) =
        TreeTableColumn<AccountListRow, String>(strings.getString(titleKey)).apply {
            this.id = id
            setCellValueFactory { features ->
                val value = when (val row = features.value?.value) {
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
    ) : TreeTableCell<AccountListRow, String>() {
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

// The account type of an account type group's row, or null for any other row.
private fun TreeItem<AccountListRow>.accountType(): AccountTypeConstant? =
    ((value as? AccountListRow.GroupRow)?.group as? AccountGroup.ByAccountType)?.accountType?.value

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
