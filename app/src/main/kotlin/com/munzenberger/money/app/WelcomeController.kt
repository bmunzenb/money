package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.stage.Stage
import java.net.URL

class WelcomeController {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("WelcomeLayout.fxml")
    }

    private val viewModel = WelcomeViewModel()

    private lateinit var stage: Stage

    fun start(stage: Stage) {
        this.stage = stage
    }

    @FXML fun onCreateDatabase() {
        viewModel.createDatabase(stage)
    }

    @FXML fun onOpenDatabase() {
        viewModel.openDatabase(stage)
    }
}
