package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.desktop.accounts.AccountConverter
import com.munzenberger.money.desktop.components.AutoCompleteComboBox
import com.munzenberger.money.desktop.components.DateConverter
import com.munzenberger.money.desktop.components.FormGrid
import com.munzenberger.money.desktop.components.LoadStateContainer
import com.munzenberger.money.desktop.components.errorLabel
import com.munzenberger.money.desktop.components.setError
import com.munzenberger.money.desktop.components.setShown
import com.munzenberger.money.desktop.components.setTextIfChanged
import com.munzenberger.money.desktop.components.smallProgressIndicator
import com.munzenberger.money.desktop.components.withError
import com.munzenberger.money.desktop.components.withProgress
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.register.decreaseTitle
import com.munzenberger.money.desktop.register.increaseTitle
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.register.RegisterAmountColumns
import com.munzenberger.money.presentation.register.registerAmountColumns
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.loadedOrEmpty
import com.munzenberger.money.presentation.transactions.TransactionDetailsUiState
import com.munzenberger.money.presentation.transactions.TransactionDetailsViewModel
import com.munzenberger.money.presentation.transactions.TransactionType
import javafx.geometry.Insets
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.koin.core.parameter.parametersOf

/** Adds a new transaction at [Route.NewTransaction], or edits an existing one at [Route.TransactionDetails]. */
class TransactionDetailsScreen(scope: ScreenScope, route: Route.TransactionFormRoute) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: TransactionDetailsViewModel = scope.viewModel { parametersOf(route) }

    // Choosing the account the state already has, as render does, leaves the ViewModel's account unchanged.
    private val accountComboBox = ComboBox<Account>().apply {
        converter = AccountConverter
        setOnAction { value?.let(viewModel::onAccountChange) }
    }

    // Setting the date the state already has, as render does, leaves the ViewModel's date unchanged. Clearing the text
    // puts back the state's date, since the form always has one.
    private val datePicker = DatePicker().apply {
        converter = DateConverter
        valueProperty().addListener { _, _, date ->
            if (date != null) {
                viewModel.onDateChange(date.toKotlinLocalDate())
            } else {
                value = viewModel.state.value.date.toJavaLocalDate()
            }
        }
    }

    // Titled for the selected account's type, as the register's amount columns are, and in the same order.
    private val decreaseRadioButton = RadioButton().apply { userData = TransactionType.Decrease }
    private val increaseRadioButton = RadioButton().apply { userData = TransactionType.Increase }

    // Selecting the type the state already has, as render does, leaves the ViewModel's type unchanged.
    private val typeToggleGroup = ToggleGroup().apply {
        toggles.setAll(decreaseRadioButton, increaseRadioButton)
        selectedToggleProperty().addListener { _, _, toggle ->
            (toggle?.userData as? TransactionType)?.let(viewModel::onTypeChange)
        }
    }

    private val typeRadioButtons = VBox(Spacing.CONTROL_SPACING, decreaseRadioButton, increaseRadioButton)

    private val numberField = TextField().apply {
        maxWidth = NUMBER_MAX_WIDTH
        textProperty().addListener { _, _, number -> viewModel.onNumberChange(number) }
    }

    // Holds the payees' names, so the user can pick an existing payee or type a new one. The ViewModel matches the text
    // to an existing payee, so picking one from the list, which sets the text, selects that payee.
    private val payeeComboBox = AutoCompleteComboBox<String>().apply {
        maxWidth = Double.MAX_VALUE
        editor.textProperty().addListener { _, _, payeeName -> viewModel.onPayeeNameChange(payeeName) }
    }

    private val payeesProgress = smallProgressIndicator()

    private val payeesError = errorLabel(strings.getString("payees_load_error_message"))

    private val accountsProgress = smallProgressIndicator()

    private val accountsError = errorLabel(strings.getString("accounts_load_error_message"))

    private val fields = FormGrid().apply {
        addField(
            strings.getString("transaction_account_label"),
            accountComboBox,
            accountComboBox.withProgress(accountsProgress).withError(accountsError),
        )
        // A radio button's text has no padding of its own, so its label takes none either.
        addField(
            strings.getString("transaction_type_label"),
            decreaseRadioButton,
            typeRadioButtons,
            labelPadding = Insets.EMPTY,
        )
        addField(strings.getString("transaction_number_label"), numberField)
        addField(strings.getString("transaction_date_label"), datePicker)
        addField(
            strings.getString("transaction_payee_label"),
            payeeComboBox,
            payeeComboBox.withProgress(payeesProgress).withError(payeesError),
        )
    }

    private val form = VBox(Spacing.CONTROL_SPACING, fields)

    private val content = LoadStateContainer(form, strings.getString("transaction_load_error_message")).apply {
        setVgrow(this, Priority.ALWAYS)
    }

    init {
        prefWidth = PREF_WIDTH
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(content)
        scope.collect(viewModel.state, ::render)
    }

    /** Goes back as the ViewModel's back action does. */
    fun onCloseRequest() = viewModel.onBackClick()

    private fun render(state: TransactionDetailsUiState) {
        // A new transaction has nothing to load.
        content.setLoadState(state.existingTransaction ?: LoadState.Loaded(Unit))

        renderAccount(state)
        datePicker.value = state.date.toJavaLocalDate()
        renderType(state)
        numberField.setTextIfChanged(state.number)
        renderPayee(state)
    }

    // The field can still take a new payee's name while the existing payees load, or if they fail to.
    private fun renderPayee(state: TransactionDetailsUiState) {
        val payeeNames = state.payees.loadedOrEmpty.map { it.name }
        if (payeeComboBox.items != payeeNames) {
            payeeComboBox.items.setAll(payeeNames)
        }
        payeeComboBox.editor.setTextIfChanged(state.payeeName)
        payeesProgress.setShown(state.payees is LoadState.Loading)
        payeesError.setShown(state.payees is LoadState.Error)
    }

    private fun renderType(state: TransactionDetailsUiState) {
        // Until an account is selected, the types take the titles for an account of any other type.
        val amountColumns = state.account?.registerAmountColumns ?: RegisterAmountColumns.Other
        decreaseRadioButton.text = amountColumns.decreaseTitle
        increaseRadioButton.text = amountColumns.increaseTitle
        typeToggleGroup.selectToggle(
            when (state.type) {
                TransactionType.Decrease -> decreaseRadioButton
                TransactionType.Increase -> increaseRadioButton
            }
        )
    }

    private fun renderAccount(state: TransactionDetailsUiState) {
        val accounts = state.accounts.loadedOrEmpty
        if (accountComboBox.items != accounts) {
            accountComboBox.items.setAll(accounts)
        }
        accountComboBox.value = state.account
        accountComboBox.isDisable = state.accounts is LoadState.Loading
        accountComboBox.promptText = if (state.accounts is LoadState.Loading) {
            strings.getString("loading_prompt")
        } else {
            null
        }
        accountsProgress.setShown(state.accounts is LoadState.Loading)

        val isLoadError = state.accounts is LoadState.Error
        accountComboBox.setError(isLoadError)
        accountsError.setShown(isLoadError)
    }

    private companion object {
        const val PREF_WIDTH = 500.0
        const val NUMBER_MAX_WIDTH = 150.0
    }
}
