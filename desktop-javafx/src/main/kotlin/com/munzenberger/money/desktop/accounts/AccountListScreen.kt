package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.desktop.components.ScreenHeading
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.accounts.AccountListUiState
import com.munzenberger.money.presentation.accounts.AccountListViewModel
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignP

/** Lists the accounts, with a heading and a row of controls to group them and add a new one. */
class AccountListScreen(scope: ScreenScope) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: AccountListViewModel = scope.viewModel()

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

    private val addAccountButton = Button(
        strings.getString("add_account_button_title"),
        FontIcon(MaterialDesignP.PLUS),
    ).apply {
        setOnAction { viewModel.onAddAccountClick() }
    }

    // Pushes the add account button to the end of the row.
    private val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }

    private val controls = HBox(Spacing.CONTROL_SPACING).apply {
        alignment = Pos.CENTER_LEFT
        children.setAll(groupingLabel, groupingComboBox, spacer, addAccountButton)
    }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(heading, controls)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AccountListUiState) {
        groupingComboBox.value = state.grouping
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
}
