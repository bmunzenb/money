package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.desktop.components.ScreenHeading
import com.munzenberger.money.desktop.components.columnWidthsKey
import com.munzenberger.money.desktop.components.saveColumnWidths
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.accounts.AccountListUiState
import com.munzenberger.money.presentation.accounts.AccountListViewModel
import com.munzenberger.money.presentation.preferences.UserPreferences
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.kordamp.ikonli.javafx.FontIcon
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.kordamp.ikonli.materialdesign2.MaterialDesignP

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

    private val nameColumn = column("name", "account_name_column_title")

    private val numberColumn = column("number", "account_number_column_title")

    private val balanceColumn = column("balance", "balance_column_title")

    // The columns stretch to fill the table's width. They start at the same width, so they share it equally.
    // Resizing a column takes the space from the balance column only, leaving the others as they are.
    private val accountTable = TreeTableView<AccountListRow>().apply {
        setVgrow(this, Priority.ALWAYS)
        columns.setAll(nameColumn, numberColumn, balanceColumn)
        columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN
        saveColumnWidths(preferences, COLUMN_WIDTHS_KEY)
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

    private fun render(state: AccountListUiState) {
        groupingComboBox.value = state.grouping
    }

    // The id identifies the column's saved width. The alignment is a style class that styles.css uses to align the
    // column's header and cells.
    private fun column(id: String, titleKey: String) =
        TreeTableColumn<AccountListRow, String>(strings.getString(titleKey)).apply {
            this.id = id
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
        val COLUMN_WIDTHS_KEY = columnWidthsKey("account.list.column.widths")
    }
}
