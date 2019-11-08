package com.munzenberger.money.app

import com.munzenberger.money.app.control.*
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
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
        val LAYOUT: URL = AccountListController::class.java.getResource("EditAccountLayout.fxml")
    }

    @FXML lateinit var container: Node
    @FXML lateinit var accountNameTextField: TextField
    @FXML lateinit var accountTypeComboBox: ComboBox<AccountType>
    @FXML lateinit var accountNumberTextField: TextField
    @FXML lateinit var bankComboBox: ComboBox<Bank>
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    private lateinit var stage: Stage

    private val viewModel = EditAccountViewModel()
    private val retainListeners = mutableListOf<ChangeListener<*>>()

    fun initialize() {

        accountNameTextField.textProperty().bindBidirectional(viewModel.accountNameProperty)

        accountTypeComboBox.apply {

            cellFactory = ListCellFactory.text { it.name }
            buttonCell = cellFactory.call(null)

            items = FXCollections.observableArrayList<AccountType>().apply {
                retainListeners += bindAsync(viewModel.accountTypesProperty)
            }

            valueProperty().bindBidirectional(viewModel.selectedAccountTypeProperty)

            retainListeners += disableProperty().bindAsyncStatus(viewModel.accountTypesProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        accountNumberTextField.textProperty().bindBidirectional(viewModel.accountNumberProperty)

        bankComboBox.apply {

            val bankConverter = BlockStringConverter({ it.name }, { Bank().apply { name = it } })

            cellFactory = ListCellFactory.text { bankConverter.toString(it) }

            items = FXCollections.observableArrayList<Bank>().apply {
                retainListeners += bindAsync(viewModel.banksProperty)
            }

            editor.textFormatter = autoCompleteTextFormatter(items, bankConverter)

            converter = ListLookupStringConverter(items, bankConverter)

            valueProperty().bindBidirectional(viewModel.selectedBankProperty)

            retainListeners += disableProperty().bindAsyncStatus(viewModel.banksProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        saveButton.disableProperty().bind(viewModel.notValidProperty)

        retainListeners += container.disableProperty().bindAsyncStatus(viewModel.saveStatusProperty, AsyncObject.Status.EXECUTING)

        viewModel.saveStatusProperty.addListener { _, _, status ->
            when (status) {
                is AsyncObject.Complete -> stage.close()
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

    @FXML fun onSaveButton() {
        viewModel.save()
    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
