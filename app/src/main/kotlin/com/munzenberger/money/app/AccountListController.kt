package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.stage.Stage
import java.net.URL

class AccountListController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("AccountListLayout.fxml")
    }

    private lateinit var stage: Stage

    fun start(stage: Stage) {
        this.stage = stage
    }

    @FXML fun onCreateAccount() {

        DialogBuilder.build(EditAccountController.LAYOUT) { stage, controller: EditAccountController ->
            stage.show()
            controller.start(stage)
        }
    }
}
