package com.munzenberger.money.app

import com.munzenberger.money.app.database.NewFileDatabaseConnector
import com.munzenberger.money.app.database.OpenFileDatabaseConnector
import com.munzenberger.money.core.MoneyDatabase
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.MenuBar
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.net.URL

class ApplicationController {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("ApplicationLayout.fxml")
    }

    @FXML lateinit var menuBar: MenuBar
    @FXML lateinit var borderPane: BorderPane

    private lateinit var stage: Stage

    fun start(stage: Stage) {
        this.stage = stage

        onDatabaseDisconnected()

        ApplicationState.observableDatabase.addListener { _, _, newValue ->
            when {
                newValue != null -> onDatabaseConnected(newValue)
                else -> onDatabaseDisconnected()
            }
        }
    }

    private fun onDatabaseConnected(database: MoneyDatabase) {
        stage.title = database.name
    }

    private fun onDatabaseDisconnected() {
        stage.title = "Money"
    }

    @FXML fun onFileNew() {
        NewFileDatabaseConnector(stage).connect()
    }

    @FXML fun onFileOpen() {
        OpenFileDatabaseConnector(stage).connect()
    }

    @FXML fun onFileQuit() {
        Platform.exit()
    }
}
