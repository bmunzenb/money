package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.stage.Stage
import java.net.URL

class EditTransfersController {

    companion object {
        val LAYOUT: URL = EditTransfersController::class.java.getResource("EditTransfersLayout.fxml")
    }

    @FXML private lateinit var tabPane: TabPane
    @FXML private lateinit var doneButton: Button

    private lateinit var stage: Stage

    private val viewModel = EditTransfersViewModel()
    private val tableControllers = mutableListOf<TransfersTableController>()

    fun initialize() {

        doneButton.disableProperty().bind(viewModel.doneDisabledProperty)
    }

    fun start(stage: Stage, transactionTypes: List<TransactionType>, transfers: MutableList<EditTransfer>) {

        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height

        transactionTypes.forEachIndexed { index, type ->

            val transfersOfType = transfers.filter {
                when (it.transactionType) {
                    type -> true
                    null -> index == 0
                    else -> false
                }
            }

            val tab = Tab().apply {
                text = type.name
            }

            FXMLLoader(TransfersTableController.LAYOUT).load { node: Node, controller: TransfersTableController ->
                tab.content = node
                controller.start(transfersOfType)
                tableControllers.add(controller)
            }

            // TODO: add rows to the grid showing totals for each transaction type

            tabPane.tabs.add(tab)
        }

        // TODO: automatically select the tab with transfers

        viewModel.start()
    }

    @FXML fun onDoneButton() {

    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
