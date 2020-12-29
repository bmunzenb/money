package com.munzenberger.money.app

import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXRegisterEntry
import com.munzenberger.money.core.Account
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.stage.Stage
import java.net.URL

class StartBalanceAccountController {

    companion object {
        val LAYOUT: URL = StartBalanceAccountController::class.java.getResource("StartBalanceAccountLayout.fxml")
    }

    @FXML lateinit var statementBalanceTextField: TextField
    @FXML lateinit var continueButton: Button

    private lateinit var stage: Stage
    private lateinit var database: ObservableMoneyDatabase
    private lateinit var account: Account

    fun initialize() {

        statementBalanceTextField.apply {
            val moneyConverter = MoneyStringConverter()
            textFormatter = TextFormatter(moneyConverter)
        }

        continueButton.disableProperty().bind(statementBalanceTextField.textFormatter.valueProperty().isNull)
    }

    fun start(stage: Stage, database: ObservableMoneyDatabase, account: Account) {
        this.stage = stage
        this.database = database
        this.account = account

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height
    }

    @FXML fun onCancelButton() {
        stage.close()
    }

    @FXML fun onContinueButton() {

    }
}
