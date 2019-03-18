package com.munzenberger.money.app

import javafx.stage.Stage
import java.net.URL

class EditAccountController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("EditAccountLayout.fxml")
    }

    private val viewModel = EditAccountViewModel()

    fun start(stage: Stage) {

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height

        viewModel.start()
    }
}
