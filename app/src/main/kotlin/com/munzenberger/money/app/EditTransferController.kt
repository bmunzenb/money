package com.munzenberger.money.app

import com.munzenberger.money.app.control.BlockStringConverter
import com.munzenberger.money.app.control.ListLookupStringConverter
import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.control.TextListCellFactory
import com.munzenberger.money.app.control.autoCompleteTextFormatter
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.app.property.toObservableList
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
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

class EditTransferController {

    companion object {
        val LAYOUT: URL = EditTransferController::class.java.getResource("EditTransferLayout.fxml")
    }

    @FXML lateinit var container: Node
    @FXML lateinit var typeComboBox: ComboBox<TransactionType>
    @FXML lateinit var datePicker: DatePicker
    @FXML lateinit var numberTextField: TextField
    @FXML lateinit var payeeComboBox: ComboBox<Payee>
    @FXML lateinit var amountTextField: TextField
    @FXML lateinit var memoTextField: TextField
    @FXML lateinit var statusLabel: Label
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    private lateinit var stage: Stage

    private val viewModel = EditTransferViewModel()

    fun initialize() {

        typeComboBox.apply {

            cellFactory = TextListCellFactory(TransactionType::name)
            buttonCell = cellFactory.call(null)

            items = viewModel.typesProperty

            valueProperty().bindBidirectional(viewModel.selectedTypeProperty)
            disableProperty().bind(viewModel.disabledProperty)
        }

        datePicker.apply {
            valueProperty().bindBidirectional(viewModel.dateProperty)
            disableProperty().bind(viewModel.disabledProperty)
        }

        numberTextField.apply {
            textProperty().bindBidirectional(viewModel.numberProperty)
            disableProperty().bind(viewModel.disabledProperty)
        }

        payeeComboBox.apply {

            val payeeConverter = BlockStringConverter(Payee::name) { Payee().apply { name = it } }

            cellFactory = TextListCellFactory(payeeConverter::toString)
            buttonCell = cellFactory.call(null)

            items = viewModel.payeesProperty.toObservableList()

            editor.textFormatter = autoCompleteTextFormatter(items, payeeConverter)

            converter = ListLookupStringConverter(items, payeeConverter)

            valueProperty().bindBidirectional(viewModel.selectedPayeeProperty)
            disableProperty().bind(viewModel.disabledProperty)
        }

        amountTextField.apply {

            val moneyConverter = MoneyStringConverter()

            textFormatter = TextFormatter(moneyConverter).apply {
                valueProperty().bindBidirectional(viewModel.amountProperty)
            }

            disableProperty().bind(viewModel.disabledProperty)
        }

        memoTextField.apply {
            textProperty().bindBidirectional(viewModel.memoProperty)
            disableProperty().bind(viewModel.disabledProperty)
        }

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

    fun start(stage: Stage, database: MoneyDatabase, transferId: Long) {

        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height

        viewModel.start(database, transferId)
    }

    @FXML fun onSaveButton() {
        viewModel.save()
    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
