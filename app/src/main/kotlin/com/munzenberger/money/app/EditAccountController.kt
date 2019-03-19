package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.MoneyDatabase
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL

class EditAccountController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("EditAccountLayout.fxml")
    }

    @FXML lateinit var accountNameTextField: TextField
    @FXML lateinit var accountTypeComboBox: ComboBox<AccountType>
    @FXML lateinit var accountNumberTextField: TextField
    @FXML lateinit var bankComboBox: ComboBox<Bank>
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    private val viewModel = EditAccountViewModel()
    private lateinit var stage: Stage

    fun initialize() {

        accountNameTextField.textProperty().bindBidirectional(viewModel.accountNameProperty)

        accountTypeComboBox.apply {

            cellFactory = ListCellFactory.string { it.name }
            buttonCell = cellFactory.call(null)

            items = FXCollections.observableArrayList<AccountType>().apply {
                bindAsync(viewModel.accountTypesProperty)
            }

            valueProperty().bindBidirectional(viewModel.selectedAccountTypeProperty)
            disableProperty().bindAsync(viewModel.accountTypesProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        accountNumberTextField.textProperty().bindBidirectional(viewModel.accountNumberProperty)

        bankComboBox.apply {

            cellFactory = ListCellFactory.string { it.name }
            buttonCell = cellFactory.call(null)

            items = FXCollections.observableArrayList<Bank>().apply {
                bindAsync(viewModel.banksProperty)
            }

            valueProperty().bindBidirectional(viewModel.selectedBankProperty)
            disableProperty().bindAsync(viewModel.banksProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        saveButton.disableProperty().bind(viewModel.notValidProperty)
    }

    fun start(stage: Stage, database: MoneyDatabase, account: Account) {

        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height

        viewModel.start(database, account)
    }

    @FXML fun onSaveButton() {

    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}