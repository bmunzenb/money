package com.munzenberger.money.app

import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.control.booleanToWaitCursor
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Statement
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.stage.Stage
import java.net.URL
import java.util.function.Consumer

class BalanceStatementController {
    companion object {
        val LAYOUT: URL = BalanceStatementController::class.java.getResource("BalanceStatementLayout.fxml")
    }

    @FXML lateinit var container: Node

    @FXML lateinit var statementClosingDatePicker: DatePicker

    @FXML lateinit var startingBalanceTextField: TextField

    @FXML lateinit var endingBalanceTextField: TextField

    @FXML lateinit var continueButton: Button

    // TODO remove reference to stage in controller
    private lateinit var stage: Stage
    private lateinit var database: ObservableMoneyDatabase
    private lateinit var account: Account
    private lateinit var callback: Consumer<Statement>

    private val viewModel = BalanceStatementViewModel()

    fun initialize() {
        statementClosingDatePicker.apply {
            valueProperty().bindBidirectional(viewModel.statementDateProperty)
        }

        startingBalanceTextField.apply {
            val moneyConverter = MoneyStringConverter()
            textFormatter =
                TextFormatter(moneyConverter).apply {
                    valueProperty().bindBidirectional(viewModel.startingBalanceProperty)
                }
        }

        endingBalanceTextField.apply {
            val moneyConverter = MoneyStringConverter()
            textFormatter =
                TextFormatter(moneyConverter).apply {
                    valueProperty().bindBidirectional(viewModel.endingBalanceProperty)
                }
        }

        continueButton.disableProperty().bind(viewModel.isInvalidBinding)

        container.disableProperty().bind(viewModel.operationInProgressProperty)
    }

    fun start(
        stage: Stage,
        database: ObservableMoneyDatabase,
        account: Account,
        callback: Consumer<Statement>,
    ) {
        this.stage = stage
        this.database = database
        this.account = account
        this.callback = callback

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height

        viewModel.operationInProgressProperty.addListener { _, _, newValue ->
            stage.scene.cursor = booleanToWaitCursor(newValue)
        }

        viewModel.start(account, database) {
            ErrorAlert.showAndWait(it)
            onCancelButton()
        }
    }

    @FXML fun onCancelButton() {
        stage.close()
    }

    @FXML fun onContinueButton() {
        viewModel.saveStatement(
            onSuccess = { onStatementReady(it) },
            onError = { ErrorAlert.showAndWait(it) },
        )
    }

    private fun onStatementReady(statement: Statement) {
        stage.close()
        callback.accept(statement)
    }
}
