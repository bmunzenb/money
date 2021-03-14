package com.munzenberger.money.app

import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Account
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.stage.Stage
import java.net.URL

class StartBalanceAccountController {

    companion object {
        val LAYOUT: URL = StartBalanceAccountController::class.java.getResource("StartBalanceAccountLayout.fxml")
    }

    @FXML lateinit var container: Node
    @FXML lateinit var statementClosingDatePicker: DatePicker
    @FXML lateinit var statementBalanceTextField: TextField
    @FXML lateinit var continueButton: Button

    private lateinit var stage: Stage
    private lateinit var database: ObservableMoneyDatabase
    private lateinit var account: Account

    private val viewModel = StartBalanceAccountViewModel()

    fun initialize() {

        statementClosingDatePicker.apply {
            valueProperty().bindBidirectional(viewModel.statementDateProperty)
            disableProperty().bindAsyncStatus(viewModel.loadStatusProperty, AsyncObject.Status.ERROR)
        }

        statementBalanceTextField.apply {
            val moneyConverter = MoneyStringConverter()
            textFormatter = TextFormatter(moneyConverter).apply {
                valueProperty().bindBidirectional(viewModel.statementBalanceProperty)
            }
            disableProperty().bindAsyncStatus(viewModel.loadStatusProperty, AsyncObject.Status.ERROR)
        }

        continueButton.disableProperty().bind(viewModel.isInvalidBinding)

        container.disableProperty().bindAsyncStatus(viewModel.loadStatusProperty,
            AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING)

        viewModel.loadStatusProperty.addListener { _, _, status ->
            when (status) {
                is AsyncObject.Error -> ErrorAlert.showAndWait(status.error)
                else -> Unit
            }
        }
    }

    fun start(stage: Stage, database: ObservableMoneyDatabase, account: Account) {
        this.stage = stage
        this.database = database
        this.account = account

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height

        viewModel.start(account, database)
    }

    @FXML fun onCancelButton() {
        stage.close()
    }

    @FXML fun onContinueButton() {
        viewModel.prepareStatement().let { statement ->
            // TODO present the balance account controller
        }
    }
}
