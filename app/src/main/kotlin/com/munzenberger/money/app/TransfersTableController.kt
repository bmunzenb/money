package com.munzenberger.money.app

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.TableView
import java.net.URL

class TransfersTableController {

    companion object {
        val LAYOUT: URL = TransfersTableController::class.java.getResource("TransfersTableLayout.fxml")
    }

    @FXML private lateinit var tableView: TableView<EditTransfer>

    fun start(transfers: List<EditTransfer>) {

        tableView.apply {

            items = FXCollections.observableArrayList<EditTransfer>(transfers)
        }
    }
}
