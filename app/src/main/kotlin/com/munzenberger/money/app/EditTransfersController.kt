package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.net.URL

class EditTransfersController {

    companion object {
        val LAYOUT: URL = EditTransfersController::class.java.getResource("EditTransfersLayout.fxml")
    }

    @FXML private lateinit var creditsContent: AnchorPane
    @FXML private lateinit var debitsContent: AnchorPane
    @FXML private lateinit var doneButton: Button

    private lateinit var creditsTableController: TransfersTableController
    private lateinit var debitsTableController: TransfersTableController

    private lateinit var stage: Stage

    private val viewModel = EditTransfersViewModel()

    fun initialize() {

        FXMLLoader(TransfersTableController.LAYOUT).load { node: Node, controller: TransfersTableController ->
            AnchorPane.setLeftAnchor(node, 0.0)
            AnchorPane.setTopAnchor(node, 0.0)
            AnchorPane.setRightAnchor(node, 0.0)
            AnchorPane.setBottomAnchor(node, 0.0)
            creditsContent.children.add(node)
            creditsTableController = controller
        }

        FXMLLoader(TransfersTableController.LAYOUT).load { node: Node, controller: TransfersTableController ->
            AnchorPane.setLeftAnchor(node, 0.0)
            AnchorPane.setTopAnchor(node, 0.0)
            AnchorPane.setRightAnchor(node, 0.0)
            AnchorPane.setBottomAnchor(node, 0.0)
            debitsContent.children.add(node)
            debitsTableController = controller
        }

        doneButton.disableProperty().bind(viewModel.doneDisabledProperty)
    }

    fun start(stage: Stage) {

        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height

        viewModel.start()
    }

    @FXML fun onDoneButton() {

    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
