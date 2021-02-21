package com.munzenberger.money.app

import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.database.ObservableMoneyDatabase
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

        statementClosingDatePicker.valueProperty().bindBidirectional(viewModel.statementDateProperty)

        statementBalanceTextField.apply {
            val moneyConverter = MoneyStringConverter()
            textFormatter = TextFormatter(moneyConverter).apply {
                valueProperty().bindBidirectional(viewModel.statementBalanceProperty)
            }
        }

        continueButton.disableProperty().bind(viewModel.isInvalidProperty)

        container.disableProperty().bind(viewModel.isLoadingProperty)
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

    }
}
