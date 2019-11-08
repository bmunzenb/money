package com.munzenberger.money.app

import com.munzenberger.money.app.control.BlockStringConverter
import com.munzenberger.money.app.control.ListCellFactory
import com.munzenberger.money.app.control.ListLookupStringConverter
import com.munzenberger.money.app.control.autoCompleteTextFormatter
import com.munzenberger.money.app.model.DelayedCategory
import com.munzenberger.money.app.model.PendingCategory
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.stage.Stage
import java.net.URL
import java.text.ParseException

class EditTransactionController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("EditTransactionLayout.fxml")
    }

    @FXML lateinit var container: Node
    @FXML lateinit var accountComboBox: ComboBox<Account>
    @FXML lateinit var typeComboBox: ComboBox<TransactionType>
    @FXML lateinit var datePicker: DatePicker
    @FXML lateinit var payeeComboBox: ComboBox<Payee>
    @FXML lateinit var categoryComboBox: ComboBox<DelayedCategory>
    @FXML lateinit var categorySplitButton: Button
    @FXML lateinit var amountTextField: TextField
    @FXML lateinit var memoTextField: TextField
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    private lateinit var stage: Stage

    private val viewModel = EditTransactionViewModel()

    fun initialize() {

        accountComboBox.apply {

            cellFactory = ListCellFactory.text(Account::name)
            buttonCell = cellFactory.call(null)

            items.bindAsync(viewModel.accountsProperty)

            valueProperty().bindBidirectional(viewModel.selectedAccountProperty)

            disableProperty().bindAsyncStatus(viewModel.accountsProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        typeComboBox.apply {

            cellFactory = ListCellFactory.text(TransactionType::name)
            buttonCell = cellFactory.call(null)

            items = viewModel.typesProperty

            valueProperty().bindBidirectional(viewModel.selectedTypeProperty)
        }

        datePicker.valueProperty().bindBidirectional(viewModel.dateProperty)

        payeeComboBox.apply {

            val payeeConverter = BlockStringConverter(Payee::name) { Payee().apply { name = it } }

            cellFactory = ListCellFactory.text(payeeConverter::toString)
            buttonCell = cellFactory.call(null)

            items.bindAsync(viewModel.payeesProperty)

            editor.textFormatter = autoCompleteTextFormatter(items, payeeConverter)

            converter = ListLookupStringConverter(items, payeeConverter)

            valueProperty().bindBidirectional(viewModel.selectedPayeeProperty)

            disableProperty().bindAsyncStatus(viewModel.payeesProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        categoryComboBox.apply {

            val categoryConverter = BlockStringConverter<DelayedCategory>(DelayedCategory::name) { PendingCategory(it) }

            cellFactory = ListCellFactory.text(categoryConverter::toString)
            buttonCell = cellFactory.call(null)

            items.bindAsync(viewModel.categoriesProperty)

            editor.textFormatter = autoCompleteTextFormatter(items, categoryConverter)

            converter = ListLookupStringConverter(items, categoryConverter)

            valueProperty().bindBidirectional(viewModel.selectedCategoryProperty)

            disableProperty().bindAsyncStatus(viewModel.categoriesProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        categorySplitButton.disableProperty().bindAsyncStatus(viewModel.categoriesProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING,
                AsyncObject.Status.ERROR)

        amountTextField.apply {

            val toMoney: (String) -> Money? = {
                try {
                    Money.valueOfFraction(it)
                } catch (e: ParseException) {
                    null
                }
            }

            val moneyConverter = BlockStringConverter(Money::toStringWithoutCurrency, toMoney)

            textFormatter = TextFormatter(moneyConverter).apply {
                valueProperty().bindBidirectional(viewModel.amountProperty)
            }
        }

        memoTextField.textProperty().bindBidirectional(viewModel.memoProperty)

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

    fun start(stage: Stage, database: MoneyDatabase, account: Account) {
        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height

        viewModel.start(database, account)
    }

    @FXML fun onCategorySplitButton() {

    }

    @FXML fun onSaveButton() {
        viewModel.save()
    }

    @FXML fun onCancelButton() {
        viewModel.close()
        stage.close()
    }
}
