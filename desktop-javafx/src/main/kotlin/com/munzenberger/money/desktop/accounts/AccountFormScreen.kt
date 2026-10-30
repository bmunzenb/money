package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.desktop.components.AutoCompleteComboBox
import com.munzenberger.money.desktop.components.ScreenHeading
import com.munzenberger.money.desktop.components.setShown
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.accounts.AccountFormUiState
import com.munzenberger.money.presentation.accounts.AccountFormViewModel
import com.munzenberger.money.presentation.accounts.LoadState
import com.munzenberger.money.presentation.accounts.SaveState
import com.munzenberger.money.presentation.accounts.loadedOrEmpty
import javafx.css.PseudoClass
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.koin.core.parameter.parametersOf

/** Adds a new account, or edits the account with [accountId] if there is one. */
class AccountFormScreen(scope: ScreenScope, accountId: AccountId?) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: AccountFormViewModel = scope.viewModel { parametersOf(accountId) }

    private val heading = ScreenHeading(
        strings.getString(if (accountId == null) "new_account_title" else "edit_account_title")
    )

    private val nameField = TextField().apply {
        promptText = strings.getString("required_prompt")
        textProperty().addListener { _, _, name -> viewModel.onNameChange(name) }
    }

    private val nameError = errorLabel("account_name_error_message")

    // Choosing the type the state already has, as render does, leaves the ViewModel's type unchanged.
    private val accountTypeComboBox = ComboBox<AccountType>().apply {
        converter = AccountTypeConverter
        maxWidth = Double.MAX_VALUE
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

    private val banksError = errorLabel("financial_institution_load_error_message")

    private val numberField = TextField().apply {
        textProperty().addListener { _, _, number -> viewModel.onNumberChange(number) }
    }

    private val currencySymbolLabel = Label()

    // Reformats the amount, or shows it as invalid, once the user leaves the field.
    private val initialBalanceField = TextField().apply {
        textProperty().addListener { _, _, balance -> viewModel.onInitialBalanceChange(balance) }
        focusedProperty().addListener { _, _, focused -> if (!focused) viewModel.onInitialBalanceFocusLost() }
    }

    private val initialBalanceError = errorLabel("initial_balance_error_message")

    private val commentsArea = TextArea().apply {
        isWrapText = true
        prefRowCount = COMMENTS_ROW_COUNT
        textProperty().addListener { _, _, memo -> viewModel.onMemoChange(memo) }
    }

    private val closedCheckBox = CheckBox(strings.getString("account_closed_label")).apply {
        selectedProperty().addListener { _, _, isClosed -> viewModel.onIsClosedChange(isClosed) }
    }

    private val fields = GridPane().apply {
        hgap = Spacing.MEDIUM
        vgap = Spacing.MEDIUM
        maxWidth = FORM_MAX_WIDTH
        // The labels' column is as wide as its widest label, never shrinking to cut one off. The fields take the rest.
        columnConstraints.setAll(
            ColumnConstraints().apply {
                minWidth = USE_PREF_SIZE
                prefWidth = USE_COMPUTED_SIZE
                maxWidth = USE_PREF_SIZE
                hgrow = Priority.NEVER
            },
            ColumnConstraints().apply { hgrow = Priority.ALWAYS },
        )
        addSection("account_section_title")
        addField("account_name_label", nameField, VBox(ERROR_SPACING, nameField, nameError))
        addField(
            "account_type_label",
            accountTypeComboBox,
            VBox(ERROR_SPACING, withProgress(accountTypeComboBox, accountTypesProgress), accountTypeError),
        )
        addField(
            "financial_institution_label",
            bankComboBox,
            VBox(ERROR_SPACING, withProgress(bankComboBox, banksProgress), banksError),
        )
        // A new account is always open, so it can only be closed once it exists. The row is left out rather than
        // hidden, since the grid would still put a gap around an empty row.
        if (accountId != null) {
            add(closedCheckBox, 1, rowCount)
        }
        addSection("balance_and_reference_section_title")
        addField("account_number_label", numberField, numberField)
        addField(
            "initial_balance_label",
            initialBalanceField,
            VBox(ERROR_SPACING, withPrefix(currencySymbolLabel, initialBalanceField), initialBalanceError),
        )
        addSection("additional_information_section_title")
        addField("comments_label", commentsArea, commentsArea)
    }

    // Only the fields scroll, so the heading and the buttons stay visible.
    private val fieldsScrollPane = ScrollPane(fields).apply {
        styleClass += "borderless-scroll-pane"
        isFitToWidth = true
        setVgrow(this, Priority.ALWAYS)
    }

    private val saveError = errorLabel("save_account_error_message")

    private val savingProgress = smallProgressIndicator()

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

    // ButtonBar orders the buttons by their data for the platform, e.g. Cancel before Save on macOS and after it on
    // Windows.
    private val buttonBar = ButtonBar().apply {
        buttons.setAll(saveButton, cancelButton)
    }

    private val footer = HBox(Spacing.CONTROL_SPACING, savingProgress, buttonBar).apply {
        alignment = Pos.CENTER_RIGHT
    }

    private val form = VBox(Spacing.CONTROL_SPACING, fieldsScrollPane, saveError, footer)

    // Shown in place of the form while the account being edited loads.
    private val loadingProgress = ProgressIndicator()

    private val loadError = Label(strings.getString("account_load_error_message"))

    private val content = StackPane(form, loadingProgress, loadError).apply {
        alignment = Pos.TOP_LEFT
        StackPane.setAlignment(loadingProgress, Pos.CENTER)
        setVgrow(this, Priority.ALWAYS)
    }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(heading, content)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AccountFormUiState) {
        form.setShown(state.isFormReady)
        loadingProgress.setShown(state.existingAccount is LoadState.Loading)
        loadError.setShown(state.existingAccount is LoadState.Error)

        // The form is locked while saving, so what's saved is what's on screen.
        val isSaving = state.saveState == SaveState.Saving
        fields.isDisable = isSaving
        saveButton.isDisable = isSaving || !state.isFormReady
        cancelButton.isDisable = isSaving
        savingProgress.setShown(isSaving)
        saveError.setShown(state.saveState == SaveState.Failed)

        nameField.setTextIfChanged(state.name)
        nameField.setError(state.isNameError)
        nameError.setShown(state.isNameError)

        renderAccountType(state)
        renderBank(state)

        numberField.setTextIfChanged(state.number)

        currencySymbolLabel.text = state.currencySymbol
        initialBalanceField.setTextIfChanged(state.initialBalance)
        initialBalanceField.setError(state.isInitialBalanceError)
        initialBalanceError.setShown(state.isInitialBalanceError)

        commentsArea.setTextIfChanged(state.memo)

        closedCheckBox.isSelected = state.isClosed
    }

    private fun renderAccountType(state: AccountFormUiState) {
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
    private fun renderBank(state: AccountFormUiState) {
        val bankNames = state.banks.loadedOrEmpty.map { it.name }
        if (bankComboBox.items != bankNames) {
            bankComboBox.items.setAll(bankNames)
        }
        bankComboBox.editor.setTextIfChanged(state.bankName)
        banksProgress.setShown(state.banks is LoadState.Loading)
        banksError.setShown(state.banks is LoadState.Error)
    }

    // Adds a row with the section's title over a separator, spanning both columns.
    private fun GridPane.addSection(titleKey: String) {
        val title = Label(strings.getString(titleKey)).apply { styleClass += "form-section-title" }
        val heading = VBox(Spacing.CONTROL_SPACING, title, Separator())
        heading.padding = Insets(Spacing.SMALL, 0.0, 0.0, 0.0)
        add(heading, 0, rowCount, 2, 1)
    }

    // Adds a row with the label for the field, and the node holding the field and anything that goes with it.
    private fun GridPane.addField(labelKey: String, field: Node, fieldNode: Node) {
        val row = rowCount
        val label = Label(strings.getString(labelKey)).apply { labelFor = field }
        GridPane.setValignment(label, VPos.TOP)
        // Lines up the label's text with the text in the field beside it.
        label.padding = Insets(LABEL_TOP_PADDING, 0.0, 0.0, 0.0)
        add(label, 0, row)
        add(fieldNode, 1, row)
    }

    private fun withProgress(field: Node, progress: Node) = HBox(Spacing.CONTROL_SPACING, field, progress).apply {
        alignment = Pos.CENTER_LEFT
        HBox.setHgrow(field, Priority.ALWAYS)
    }

    private fun withPrefix(prefix: Node, field: Node) = HBox(Spacing.CONTROL_SPACING, prefix, field).apply {
        alignment = Pos.CENTER_LEFT
        HBox.setHgrow(field, Priority.ALWAYS)
    }

    private object AccountTypeConverter : StringConverter<AccountType>() {
        override fun toString(accountType: AccountType?): String = accountType?.value?.label.orEmpty()

        // The combo box isn't editable, so it never converts text back to an account type.
        override fun fromString(string: String?): AccountType? = null
    }

    private companion object {
        const val FORM_MAX_WIDTH = 560.0
        const val ERROR_SPACING = 4.0
        const val LABEL_TOP_PADDING = 4.0
        const val SMALL_PROGRESS_SIZE = 16.0
        const val COMMENTS_ROW_COUNT = 4
        val ERROR_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("error")

        fun errorLabel(messageKey: String? = null) = Label(messageKey?.let(strings::getString)).apply {
            styleClass += "error-message"
            isWrapText = true
        }

        fun smallProgressIndicator() = ProgressIndicator().apply {
            setPrefSize(SMALL_PROGRESS_SIZE, SMALL_PROGRESS_SIZE)
            setMaxSize(SMALL_PROGRESS_SIZE, SMALL_PROGRESS_SIZE)
        }

        // Setting the text the field already has would move the caret, so a field's own change isn't set back on it.
        fun TextInputControl.setTextIfChanged(text: String) {
            if (this.text != text) this.text = text
        }

        fun Node.setError(isError: Boolean) = pseudoClassStateChanged(ERROR_PSEUDO_CLASS, isError)
    }
}
