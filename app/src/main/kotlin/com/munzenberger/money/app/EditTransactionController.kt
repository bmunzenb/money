package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.stage.Stage
import java.net.URL

class EditTransactionController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("EditTransactionLayout.fxml")
    }

    @FXML lateinit var container: Node
    @FXML lateinit var saveButton: Button
    @FXML lateinit var cancelButton: Button

    private lateinit var stage: Stage

    fun start(stage: Stage) {
        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height
        stage.maxHeight = stage.height
    }

    @FXML fun onSaveButton() {

    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
