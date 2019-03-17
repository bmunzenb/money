package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.net.URL

class ApplicationController : DatabaseConnectorDelegate {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("ApplicationLayout.fxml")
    }

    @FXML lateinit var container: VBox
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

        container.disableProperty().bind(viewModel.isConnectionInProgressProperty)
    }

    fun start(stage: Stage) {
        this.stage = stage

        stage.titleProperty().bind(viewModel.titleProperty)

        presentWelcome()
    }

    @FXML override fun onCreateDatabase() {
        viewModel.createDatabase(stage)
    }

    @FXML override fun onOpenDatabase() {
        viewModel.openDatabase(stage)
    }

    @FXML fun onExit() {
        viewModel.exit()
    }

    private fun presentWelcome() {

        val loader = FXMLLoader(WelcomeController.LAYOUT)
        val view: Parent = loader.load()
        val controller: WelcomeController = loader.getController()

        controller.start(this)

        borderPane.center = view
    }

    private fun presentNavigation() {

        val loader = FXMLLoader(NavigationController.LAYOUT)
        val view: Parent = loader.load()
        val controller: NavigationController = loader.getController()

        controller.start()

        borderPane.center = view
    }
}
