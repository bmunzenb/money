package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.desktop.components.ScreenHeading
import com.munzenberger.money.desktop.components.smallProgressIndicator
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.accounts.AccountListUiState
import com.munzenberger.money.presentation.accounts.AccountListViewModel
import com.munzenberger.money.presentation.preferences.UserPreferences
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.loadedOrEmpty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
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

    private val newAccountButton = Button(strings.getString("new_account_button_title")).apply {
        setOnAction { viewModel.onNewAccountClick() }
    }

    // Pushes the new account button to the end of the row.
    private val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }

    private val controls = HBox(Spacing.CONTROL_SPACING).apply {
        alignment = Pos.CENTER_LEFT
        children.setAll(groupingLabel, groupingComboBox, spacer, newAccountButton)
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
            viewModel.onNewAccountClick()
        }
    }

    private val accountTable = AccountListTable(
        preferences = preferences,
        onAccountClick = viewModel::onAccountClick,
        onAccountSettingsClick = viewModel::onAccountSettingsClick,
    ).apply {
        setVgrow(this, Priority.ALWAYS)
    }

    private val totalBalanceLabel = Label(strings.getString("total_balance_label"))

    // The total balance's value while it loads, once it has, and when it can't be calculated.
    private val totalBalanceProgress = smallProgressIndicator()
    private val totalBalanceAmount = Label()
    private val totalBalanceErrorMessage = balanceErrorMessage()

    // Stays at the bottom of the screen, since the table above it takes the rest of the height. Its labels inherit the
    // bold font.
    private val totalBalance = HBox(Spacing.CONTROL_SPACING).apply {
        alignment = Pos.CENTER_RIGHT
        styleClass += "total-balance"
    }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(heading, controls, accountTable, totalBalance)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AccountListUiState) {
        groupingComboBox.value = state.grouping

        accountTable.placeholder = when (state.groups) {
            LoadState.Loading -> loadingProgress
            LoadState.Error -> loadErrorMessage
            is LoadState.Loaded -> emptyMessage
        }

        totalBalance.isVisible = state.groups.loadedOrEmpty.isNotEmpty()
        totalBalance.children.setAll(
            totalBalanceLabel,
            when (val total = state.balances.total) {
                LoadState.Loading -> totalBalanceProgress
                LoadState.Error -> totalBalanceErrorMessage
                is LoadState.Loaded -> totalBalanceAmount.apply { text = total.value.toAccountString() }
            },
        )

        // A state that changes only the grouping leaves the table's rows as they are.
        accountTable.setGroups(state.groups.loadedOrEmpty)
        accountTable.setBalances(state.balances)
    }
}
