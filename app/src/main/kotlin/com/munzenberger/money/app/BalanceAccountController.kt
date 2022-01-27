package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.stage.Stage
import java.net.URL

class BalanceAccountController  {

    companion object {
        val LAYOUT: URL = BalanceAccountController::class.java.getResource("BalanceAccountLayout.fxml")!!
    }

    @FXML lateinit var continueButton: Button
    @FXML lateinit var cancelButton: Button

    private val viewModel = BalanceAccountViewModel()

    private lateinit var stage: Stage

    fun initialize() {

    }

    fun start(stage: Stage) {

        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height
    }

    @FXML fun onContinueButton() {

    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
