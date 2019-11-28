package com.munzenberger.money.app

import com.munzenberger.money.app.model.DelayedCategory
import javafx.collections.FXCollections
import javafx.collections.ObservableList
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

    @FXML private lateinit var anchorPane: AnchorPane
    @FXML private lateinit var doneButton: Button

    private lateinit var stage: Stage
    private lateinit var transfers: ObservableList<EditTransfer>
    private lateinit var copiedTransfers: ObservableList<EditTransfer>

    private val viewModel = EditTransfersViewModel()
    private val tableControllers = mutableListOf<TransfersTableController>()

    fun initialize() {

        doneButton.disableProperty().bind(viewModel.doneDisabledProperty)
    }

    fun start(stage: Stage, transfers: ObservableList<EditTransfer>, categories: List<DelayedCategory>) {

        this.stage = stage
        this.transfers = transfers

        stage.minWidth = stage.width
        stage.minHeight = stage.height

        // operate on a copy of the transfer list and only apply the changes if the user clicks Done
        copiedTransfers = transfers
                .map { EditTransfer.from(it) }
                .let { FXCollections.observableList(it) }

        FXMLLoader(TransfersTableController.LAYOUT).load { node: Node, controller: TransfersTableController ->
            AnchorPane.setLeftAnchor(node, 0.0)
            AnchorPane.setTopAnchor(node, 0.0)
            AnchorPane.setRightAnchor(node, 0.0)
            AnchorPane.setBottomAnchor(node, 0.0)

            anchorPane.children.add(node)
            controller.start(copiedTransfers, categories)
            tableControllers.add(controller)
        }

        viewModel.start()
    }

    @FXML fun onDoneButton() {

    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
