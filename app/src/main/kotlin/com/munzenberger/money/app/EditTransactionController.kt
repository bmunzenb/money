package com.munzenberger.money.app

import com.munzenberger.money.app.control.BlockStringConverter
import com.munzenberger.money.app.control.ListLookupStringConverter
import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.control.TextListCellFactory
import com.munzenberger.money.app.control.autoCompleteTextFormatter
import com.munzenberger.money.app.model.TransactionCategory
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.app.property.toObservableList
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.Transaction
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.stage.Stage
import java.net.URL

class EditTransactionController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("EditTransactionLayout.fxml")
    }

    @FXML lateinit var container: Node
    @FXML lateinit var accountComboBox: ComboBox<Account>
    @FXML lateinit var typeComboBox: ComboBox<TransactionType>
    @FXML lateinit var datePicker: DatePicker
    @FXML lateinit var numberTextField: TextField
    @FXML lateinit var payeeComboBox: ComboBox<Payee>
    @FXML lateinit var categoryComboBox: ComboBox<TransactionCategory>
    @FXML lateinit var categorySplitButton: Button
    @FXML lateinit var amountTextField: TextField
    @FXML lateinit var memoTextField: TextField
    @FXML lateinit var statusLabel: Label
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    // TODO remove reference to stage in controller
    private lateinit var stage: Stage

    private val viewModel = EditTransactionViewModel()

    fun initialize() {

        accountComboBox.apply {

            cellFactory = TextListCellFactory(Account::name)
            buttonCell = cellFactory.call(null)

            items = viewModel.accountsProperty.toObservableList()

            valueProperty().bindBidirectional(viewModel.selectedAccountProperty)

            disableProperty().bindAsyncStatus(viewModel.accountsProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        typeComboBox.apply {

            cellFactory = TextListCellFactory(TransactionType::name)
            buttonCell = cellFactory.call(null)

            items = viewModel.typesProperty

            valueProperty().bindBidirectional(viewModel.selectedTypeProperty)

            disableProperty().bind(viewModel.typeDisabledProperty)
        }

        datePicker.valueProperty().bindBidirectional(viewModel.dateProperty)

        numberTextField.textProperty().bindBidirectional(viewModel.numberProperty)

        payeeComboBox.apply {

            val payeeConverter = BlockStringConverter(Payee::name) { Payee().apply { name = it } }

            cellFactory = TextListCellFactory(payeeConverter::toString)
            buttonCell = cellFactory.call(null)

            items = viewModel.payeesProperty.toObservableList()

            editor.textFormatter = autoCompleteTextFormatter(items, payeeConverter)

            converter = ListLookupStringConverter(items, payeeConverter)

            valueProperty().bindBidirectional(viewModel.selectedPayeeProperty)

            disableProperty().bindAsyncStatus(viewModel.payeesProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        categoryComboBox.apply {

            val categoryConverter = BlockStringConverter<TransactionCategory>(TransactionCategory::name) {
                TransactionCategory.Pending(it)
            }

            cellFactory = TextListCellFactory(categoryConverter::toString)
            buttonCell = cellFactory.call(null)

            items = viewModel.categoriesProperty.toObservableList()

            editor.textFormatter = autoCompleteTextFormatter(items, categoryConverter)

            converter = ListLookupStringConverter(items, categoryConverter)

            valueProperty().bindBidirectional(viewModel.selectedCategoryProperty)

            disableProperty().bind(viewModel.categoryDisabledProperty)
        }

        categorySplitButton.disableProperty().bind(viewModel.splitDisabledProperty)

        amountTextField.apply {

            val moneyConverter = MoneyStringConverter()

            textFormatter = TextFormatter(moneyConverter).apply {
                valueProperty().bindBidirectional(viewModel.amountProperty)
            }

            disableProperty().bind(viewModel.amountDisabledProperty)
        }

        memoTextField.textProperty().bindBidirectional(viewModel.memoProperty)

        statusLabel.textProperty().bind(viewModel.transactionStatusProperty)

        saveButton.disableProperty().bind(viewModel.notValidProperty)

        container.disableProperty().bindAsyncStatus(viewModel.saveStatusProperty, AsyncObject.Status.EXECUTING)

        viewModel.saveStatusProperty.addListener { _, _, status ->
            when (status) {
                is AsyncObject.Complete -> onCancelButton()
                is AsyncObject.Error -> ErrorAlert.showAndWait(status.error)
                else -> {}
            }
        }
    }

    fun start(stage: Stage, database: MoneyDatabase, transaction: Transaction) {
        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height

        viewModel.start(database, transaction) {
            ErrorAlert.showAndWait(it)
            onCancelButton()
        }
    }

    @FXML fun onCategorySplitButton() {

        viewModel.prepareSplit { editors, categories ->
            DialogBuilder.build(SplitTransactionController.LAYOUT) { stage, controller: SplitTransactionController ->
                stage.title = "Split Transaction"
                stage.show()
                controller.start(stage, editors, categories)
            }
        }
    }

    @FXML fun onSaveButton() {
        viewModel.save()
    }

    @FXML fun onCancelButton() {
        viewModel.close()
        stage.close()
    }
}
