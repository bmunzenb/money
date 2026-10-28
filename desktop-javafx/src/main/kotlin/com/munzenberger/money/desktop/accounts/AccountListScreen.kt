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
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Lists the accounts, with a heading and a row of controls to group them and add a new one. */
class AccountListScreen(scope: ScreenScope) : VBox(Spacing.CONTROL_SPACING), KoinComponent {
    private val viewModel: AccountListViewModel = scope.viewModel()

    private val preferences: UserPreferences by inject()

    private val heading = ScreenHeading(strings.getString("account_list_title"))

    // Choosing the grouping the state already has, as render does, leaves the ViewModel's grouping unchanged.
    private val groupingComboBox = ComboBox<AccountGrouping>().apply {
        items.setAll(AccountGrouping.entries)
        converter = AccountGroupingConverter
        setOnAction { value?.let(viewModel::onGroupingChange) }
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

    private val numberColumn = column("number", "account_number_column_title") { maskAccountNumber(it.number) }

    // Empty until account balances are available.
    private val balanceColumn = column("balance", "balance_column_title") { "" }

    // The columns stretch to fill the table's width. They start at the same width, so they share it equally.
    // Resizing a column takes the space from the last column only, leaving the others as they are.
    // Each group is a top-level row under the hidden root, with its accounts' rows under it.
    private val accountTable = TreeTableView<AccountListRow>(TreeItem()).apply {
        setVgrow(this, Priority.ALWAYS)
        isShowRoot = false
        // Keeps the disclosure arrow beside the group's title when the user moves the columns.
        treeColumn = nameColumn
        columns.setAll(nameColumn, numberColumn, balanceColumn)
        columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        saveColumnLayout(preferences, COLUMN_LAYOUT_KEY)
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
        }
    }

    private fun groupItem(group: AccountGroup) =
        TreeItem<AccountListRow>(AccountListRow.GroupRow(group)).apply {
            isExpanded = true
            children.setAll(group.accounts.map { TreeItem(AccountListRow.AccountRow(it)) })
        }

    // The id identifies the column's saved layout. A group's row is empty in the columns without a group value.
    private fun column(
        id: String,
        titleKey: String,
        groupValue: (AccountGroup) -> String = { "" },
        accountValue: (Account) -> String,
    ) =
        TreeTableColumn<AccountListRow, String>(strings.getString(titleKey)).apply {
            this.id = id
            setCellValueFactory { features ->
                val value = when (val row = features.value?.value) {
                    is AccountListRow.GroupRow -> groupValue(row.group)
                    is AccountListRow.AccountRow -> accountValue(row.account)
                    null -> ""
                }
                ReadOnlyStringWrapper(value)
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
    }
}

private fun AccountGroup.title(): String = when (this) {
    is AccountGroup.All -> strings.getString("account_group_all_title")
    is AccountGroup.ByAccountType -> strings.getString(
        when (accountType.value) {
            AccountTypeConstant.Savings -> "account_type_savings"
            AccountTypeConstant.Checking -> "account_type_checking"
            AccountTypeConstant.Asset -> "account_type_asset"
            AccountTypeConstant.Cash -> "account_type_cash"
            AccountTypeConstant.Credit -> "account_type_credit"
            AccountTypeConstant.Loan -> "account_type_loan"
        }
    )
    is AccountGroup.ByAccountClass -> strings.getString(
        when (accountClass.value) {
            AccountClassConstant.Assets -> "account_class_assets"
            AccountClassConstant.Liabilities -> "account_class_liabilities"
        }
    )
    is AccountGroup.ByBank -> bank?.name ?: strings.getString("account_group_no_bank_title")
}
