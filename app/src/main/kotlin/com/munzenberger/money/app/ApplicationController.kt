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

    data class Parameters(val stage: Stage)

    @FXML lateinit var menuBar: MenuBar
    @FXML lateinit var borderPane: BorderPane

    private lateinit var params: Parameters

    private var database: MoneyDatabase? = null

    fun start(params: Parameters) {
        this.params = params
    }

    @FXML fun onFileNew() {
        NewFileDatabaseConnector(params.stage).connect {
            onDatabaseConnected(it)
        }
    }

    @FXML fun onFileOpen() {
        OpenFileDatabaseConnector(params.stage).connect {
            onDatabaseConnected(it)
        }
    }

    @FXML fun onFileQuit() {
        Platform.exit()
    }

    private fun onDatabaseConnected(database: MoneyDatabase) {
        this.database?.close()
        this.database = database

        params.stage.title = database.name
    }
}
