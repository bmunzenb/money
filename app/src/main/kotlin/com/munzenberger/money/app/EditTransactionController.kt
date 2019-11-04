package com.munzenberger.money.app

import com.munzenberger.money.app.control.ListLookupStringConverter
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
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
    @FXML lateinit var payeeComboBox: ComboBox<Payee>
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    private lateinit var stage: Stage

    private val viewModel = EditTransactionViewModel()
    private val retainListeners = mutableListOf<ChangeListener<*>>()

    fun initialize() {

        accountComboBox.apply {

            cellFactory = ListCellFactory.text { it.name }
            buttonCell = cellFactory.call(null)

            items = FXCollections.observableArrayList<Account>().apply {
                retainListeners += bindAsync(viewModel.accountsProperty)
            }

            valueProperty().bindBidirectional(viewModel.selectedAccountProperty)

            retainListeners += disableProperty().bindAsyncStatus(viewModel.accountsProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
        }

        typeComboBox.apply {

            cellFactory = ListCellFactory.text { it.name }
            buttonCell = cellFactory.call(null)

            items = viewModel.typesProperty

            valueProperty().bindBidirectional(viewModel.selectedTypeProperty)
        }

        datePicker.valueProperty().bindBidirectional(viewModel.date)

        payeeComboBox.apply {

            cellFactory = ListCellFactory.text { it.name }
            buttonCell = cellFactory.call(null)

            items = FXCollections.observableArrayList<Payee>().apply {
                retainListeners += bindAsync(viewModel.payeesProperty)
            }

            converter = ListLookupStringConverter(items, { it.name }, { Payee().apply { name = it } })

            valueProperty().bindBidirectional(viewModel.selectedPayeeProperty)

            retainListeners += disableProperty().bindAsyncStatus(viewModel.accountsProperty,
                    AsyncObject.Status.PENDING,
                    AsyncObject.Status.EXECUTING,
                    AsyncObject.Status.ERROR)
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

    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
