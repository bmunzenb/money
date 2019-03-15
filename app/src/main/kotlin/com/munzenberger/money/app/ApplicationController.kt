package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.net.URL

class ApplicationController {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("ApplicationLayout.fxml")
    }

    @FXML lateinit var borderPane: BorderPane

    private val viewModel = ApplicationViewModel()

    private lateinit var stage: Stage

    @FXML fun initialize() {

        MoneyApplication.observableDatabase.addListener { _, _, db ->
            when {
                db != null -> presentNavigation()
                else -> presentWelcome()
            }
        }
    }

    fun start(stage: Stage) {
        this.stage = stage

        stage.titleProperty().bind(viewModel.titleProperty)

        presentWelcome()
    }

    @FXML fun onFileNew() {
        viewModel.createDatabase(stage)
    }

    @FXML fun onFileOpen() {
        viewModel.openDatabase(stage)
    }

    @FXML fun onFileExit() {
        viewModel.exit()
    }

    private fun presentWelcome() {

        val loader = FXMLLoader(WelcomeController.LAYOUT)
        val view: Parent = loader.load()
        val controller: WelcomeController = loader.getController()

        controller.start(stage)

        borderPane.center = view
    }

    private fun presentNavigation() {

        borderPane.center = null
    }
}
