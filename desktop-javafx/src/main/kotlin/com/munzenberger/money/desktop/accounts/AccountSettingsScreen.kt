package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.desktop.components.AutoCompleteComboBox
import com.munzenberger.money.desktop.components.FormFooter
import com.munzenberger.money.desktop.components.FormGrid
import com.munzenberger.money.desktop.components.LoadStateContainer
import com.munzenberger.money.desktop.components.errorLabel
import com.munzenberger.money.desktop.components.setError
import com.munzenberger.money.desktop.components.setShown
import com.munzenberger.money.desktop.components.setTextIfChanged
import com.munzenberger.money.desktop.components.smallProgressIndicator
import com.munzenberger.money.desktop.components.withError
import com.munzenberger.money.desktop.components.withProgress
import com.munzenberger.money.desktop.getString
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.accounts.AccountSettingsUiState
import com.munzenberger.money.presentation.accounts.AccountSettingsViewModel
import com.munzenberger.money.presentation.accounts.SaveState
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.loadedOrEmpty
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.koin.core.parameter.parametersOf

/** Adds a new account at [Route.NewAccount], or edits an existing one at [Route.AccountSettings]. */
class AccountSettingsScreen(scope: ScreenScope, route: Route.AccountFormRoute) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: AccountSettingsViewModel = scope.viewModel { parametersOf(route) }

    private val isExistingAccount = route is Route.AccountSettings

    private val nameField = TextField().apply {
        promptText = strings.getString("required_prompt")
        textProperty().addListener { _, _, name -> viewModel.onNameChange(name) }
    }

    private val nameError = errorLabel(strings.getString("account_name_error_message"))

    // Choosing the type the state already has, as render does, leaves the ViewModel's type unchanged.
    private val accountTypeComboBox = ComboBox<AccountType>().apply {
        converter = AccountTypeConverter
        setOnAction { value?.let(viewModel::onAccountTypeChange) }
    }

    private val accountTypesProgress = smallProgressIndicator()

    private val accountTypeError = errorLabel()

    // Holds the banks' names, so the user can pick an existing bank or type a new one. The ViewModel matches the text
    // to an existing bank, so picking one from the list, which sets the text, selects that bank.
    private val bankComboBox = AutoCompleteComboBox<String>().apply {
        maxWidth = Double.MAX_VALUE
        editor.textProperty().addListener { _, _, bankName -> viewModel.onBankNameChange(bankName) }
    }

    private val banksProgress = smallProgressIndicator()

    private val banksError = errorLabel(strings.getString("financial_institution_load_error_message"))

    private val numberField = TextField().apply {
        textProperty().addListener { _, _, number -> viewModel.onNumberChange(number) }
    }

    private val currencyField = Label()

    // Reformats the amount, or shows it as invalid, once the user leaves the field.
    private val initialBalanceField = TextField().apply {
        textProperty().addListener { _, _, balance -> viewModel.onInitialBalanceChange(balance) }
        focusedProperty().addListener { _, _, focused -> if (!focused) viewModel.onInitialBalanceFocusLost() }
        maxWidth = INITIAL_BALANCE_FIELD_MAX_WIDTH
    }

    private val initialBalanceError = errorLabel(strings.getString("initial_balance_error_message"))

    private val commentsArea = TextArea().apply {
        isWrapText = true
        prefRowCount = COMMENTS_ROW_COUNT
        textProperty().addListener { _, _, memo -> viewModel.onMemoChange(memo) }
    }

    private val closedCheckBox = CheckBox(strings.getString("account_closed_label")).apply {
        selectedProperty().addListener { _, _, isClosed -> viewModel.onIsClosedChange(isClosed) }
    }

    private val fields = FormGrid().apply {
        addField(strings.getString("account_name_label"), nameField, nameField.withError(nameError))
        addField(
            strings.getString("account_type_label"),
            accountTypeComboBox,
            accountTypeComboBox.withProgress(accountTypesProgress).withError(accountTypeError),
        )
        addField(
            strings.getString("financial_institution_label"),
            bankComboBox,
            bankComboBox.withProgress(banksProgress).withError(banksError),
        )
        addField(strings.getString("account_number_label"), numberField)
        addField(strings.getString("account_currency_label"), currencyField, labelPadding = Insets.EMPTY)
        addField(
            strings.getString("initial_balance_label"),
            initialBalanceField,
            initialBalanceField.withError(initialBalanceError),
        )
        // A new account is always open, so it can only be closed once it exists. The row is left out rather than
        // hidden, since the grid would still put a gap around an empty row.
        if (isExistingAccount) {
            add(closedCheckBox, 1, rowCount)
        }
        addField(strings.getString("comments_label"), commentsArea)
    }

    private val saveError = errorLabel(strings.getString("save_account_error_message"))

    private val saveButton = Button(strings.getString("save_button_title")).apply {
        isDefaultButton = true
        ButtonBar.setButtonData(this, ButtonBar.ButtonData.OK_DONE)
        setOnAction { viewModel.onSaveClick() }
    }

    private val cancelButton = Button(strings.getString("cancel_button_title")).apply {
        isCancelButton = true
        ButtonBar.setButtonData(this, ButtonBar.ButtonData.CANCEL_CLOSE)
        setOnAction { viewModel.onBackClick() }
    }

    private val footer = FormFooter(fields, saveButton, cancelButton)

    // The fields take the form's extra height, so the save error and the footer stay at the bottom of the screen.
    private val form = VBox(Spacing.CONTROL_SPACING, fields, saveError, footer).apply {
        setVgrow(fields, Priority.ALWAYS)
    }

    // Shows the form once the account being edited loads.
    private val content = LoadStateContainer(form, strings.getString("account_load_error_message")).apply {
        setVgrow(this, Priority.ALWAYS)
    }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(content)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AccountSettingsUiState) {
        // A new account has nothing to load.
        content.setLoadState(state.existingAccount ?: LoadState.Loaded(Unit))

        // The form is locked while saving, so what's saved is what's on screen.
        val isSaving = state.saveState == SaveState.Saving
        fields.isDisable = isSaving
        saveButton.isDisable = isSaving || !state.isFormReady
        cancelButton.isDisable = isSaving
        footer.setProgressShown(isSaving)
        saveError.setShown(state.saveState == SaveState.Failed)

        nameField.setTextIfChanged(state.name)
        nameField.setError(state.isNameError)
        nameError.setShown(state.isNameError)

        renderAccountType(state)
        renderBank(state)

        numberField.setTextIfChanged(state.number)

        currencyField.text = strings.getString(
            "account_currency_value",
            state.currency.displayName,
            state.currency.currencyCode,
        )

        initialBalanceField.setTextIfChanged(state.initialBalance)
        initialBalanceField.setError(state.isInitialBalanceError)
        initialBalanceError.setShown(state.isInitialBalanceError)

        commentsArea.setTextIfChanged(state.memo)

        closedCheckBox.isSelected = state.isClosed
    }

    private fun renderAccountType(state: AccountSettingsUiState) {
        val accountTypes = state.accountTypes.loadedOrEmpty
        if (accountTypeComboBox.items != accountTypes) {
            accountTypeComboBox.items.setAll(accountTypes)
        }
        accountTypeComboBox.value = state.accountType
        accountTypeComboBox.isDisable = state.accountTypes is LoadState.Loading
        accountTypeComboBox.promptText = if (state.accountTypes is LoadState.Loading) {
            strings.getString("loading_prompt")
        } else {
            null
        }
        accountTypesProgress.setShown(state.accountTypes is LoadState.Loading)

        // A failed load leaves nothing to choose, so its message takes the place of the missing type's.
        val isLoadError = state.accountTypes is LoadState.Error
        accountTypeComboBox.setError(isLoadError || state.isAccountTypeError)
        accountTypeError.text = strings.getString(
            if (isLoadError) "account_type_load_error_message" else "account_type_error_message"
        )
        accountTypeError.setShown(isLoadError || state.isAccountTypeError)
    }

    // The field can still take a new bank's name while the existing banks load, or if they fail to.
    private fun renderBank(state: AccountSettingsUiState) {
        val bankNames = state.banks.loadedOrEmpty.map { it.name }
        if (bankComboBox.items != bankNames) {
            bankComboBox.items.setAll(bankNames)
        }
        bankComboBox.editor.setTextIfChanged(state.bankName)
        banksProgress.setShown(state.banks is LoadState.Loading)
        banksError.setShown(state.banks is LoadState.Error)
    }

    private companion object {
        const val INITIAL_BALANCE_FIELD_MAX_WIDTH = 150.0
        const val COMMENTS_ROW_COUNT = 4
    }
}
