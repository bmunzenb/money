package com.munzenberger.money.app

import com.munzenberger.money.app.control.BlockStringConverter
import com.munzenberger.money.app.control.ListLookupStringConverter
import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.control.TextListCellFactory
import com.munzenberger.money.app.control.autoCompleteTextFormatter
import com.munzenberger.money.app.model.name
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.app.property.toObservableList
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.MoneyDatabase
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.stage.Stage
import java.net.URL

class EditAccountController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("EditAccountLayout.fxml")!!
    }

    @FXML lateinit var container: Node
    @FXML lateinit var accountNameTextField: TextField
    @FXML lateinit var accountTypeComboBox: ComboBox<AccountType>
    @FXML lateinit var accountNumberTextField: TextField
    @FXML lateinit var bankComboBox: ComboBox<Bank>
    @FXML lateinit var initialBalanceTextField: TextField
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    // TODO remove reference to stage in controller
    private lateinit var stage: Stage

    private val viewModel = EditAccountViewModel()

    fun initialize() {

        accountNameTextField.textProperty().bindBidirectional(viewModel.accountNameProperty)

        accountTypeComboBox.apply {

            cellFactory = TextListCellFactory { it.name }
            buttonCell = cellFactory.call(null)

            items = viewModel.accountTypesProperty.toObservableList()

            valueProperty().bindBidirectional(viewModel.selectedAccountTypeProperty)

            disableProperty().bindAsyncStatus(viewModel.accountTypesProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        accountNumberTextField.textProperty().bindBidirectional(viewModel.accountNumberProperty)

        bankComboBox.apply {

            val bankConverter = BlockStringConverter(Bank::name) { Bank().apply { name = it } }

            cellFactory = TextListCellFactory(bankConverter::toString)

            items = viewModel.banksProperty.toObservableList()

            editor.textFormatter = autoCompleteTextFormatter(items, bankConverter)

            converter = ListLookupStringConverter(items, bankConverter)

            valueProperty().bindBidirectional(viewModel.selectedBankProperty)

            disableProperty().bindAsyncStatus(viewModel.banksProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        initialBalanceTextField.apply {

            val moneyConverter = MoneyStringConverter()

            textFormatter = TextFormatter(moneyConverter).apply {
                valueProperty().bindBidirectional(viewModel.initialBalanceProperty)
            }
        }

        saveButton.disableProperty().bind(viewModel.notValidProperty)

        container.disableProperty().bind(viewModel.isOperationInProgressProperty)
    }

    fun start(stage: Stage, database: MoneyDatabase, account: Account) {

        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height

        viewModel.start(database, account)
    }

    @FXML fun onSaveButton() {
        viewModel.save {
            when (it) {
                null -> close()
                else -> ErrorAlert.showAndWait(it)
            }
        }
    }

    @FXML fun onCancelButton() {
        close()
    }

    private fun close() {
        stage.close()
    }
}
