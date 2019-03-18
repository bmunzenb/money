package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.MenuBar
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.net.URL

class ApplicationController : DatabaseConnectorDelegate {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("ApplicationLayout.fxml")
    }

    @FXML lateinit var menuBar: MenuBar
    @FXML lateinit var contentPane: AnchorPane

    private val viewModel = ApplicationViewModel()

    private lateinit var stage: Stage

    @FXML fun initialize() {

        menuBar.isUseSystemMenuBar = true

        viewModel.connectedDatabaseProperty.addListener { _, _, db ->
            when {
                db != null -> presentNavigation()
                else -> presentWelcome()
            }
        }

        menuBar.disableProperty().bind(viewModel.isConnectionInProgressProperty)
        contentPane.disableProperty().bind(viewModel.isConnectionInProgressProperty)
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

    fun shutdown() {
        viewModel.shutdown()
    }

    private fun presentWelcome() {

        FXMLLoader(WelcomeController.LAYOUT).load { node: Node, controller: WelcomeController ->
            controller.start(this)
            setContent(node)
        }
    }

    private fun presentNavigation() {

        FXMLLoader(NavigationController.LAYOUT).load { node: Node, controller: NavigationController ->
            controller.start(stage)
            setContent(node)
        }
    }

    private fun setContent(content: Node) {

        contentPane.children.also {
            it.clear()
            it.add(content)
        }

        AnchorPane.setTopAnchor(content, 0.0)
        AnchorPane.setLeftAnchor(content, 0.0)
        AnchorPane.setRightAnchor(content, 0.0)
        AnchorPane.setBottomAnchor(content, 0.0)
    }
}
