package com.munzenberger.money.app

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.stage.Stage
import java.net.URL

class AccountListController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("AccountListLayout.fxml")
    }

    @FXML lateinit var createAccountButton: Button

    private lateinit var stage: Stage
    private lateinit var database: MoneyDatabase

    fun start(stage: Stage, database: MoneyDatabase) {
        this.stage = stage
        this.database = database
    }

    @FXML fun onCreateAccount() {

        DialogBuilder.build(EditAccountController.LAYOUT) { stage, controller: EditAccountController ->
            stage.title = createAccountButton.text
            stage.show()
            controller.start(stage, database, Account())
        }
    }
}
