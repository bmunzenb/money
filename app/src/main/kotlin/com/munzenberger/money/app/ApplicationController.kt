package com.munzenberger.money.app

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.MenuBar
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.net.URL

class ApplicationController : DatabaseConnectorDelegate, AutoCloseable {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("ApplicationLayout.fxml")
    }

    @FXML lateinit var menuBar: MenuBar
    @FXML lateinit var contentPane: AnchorPane

    private val viewModel = ApplicationViewModel()
    private var activeController: AutoCloseable? = null

    private lateinit var stage: Stage

    @FXML fun initialize() {

        menuBar.isUseSystemMenuBar = true

        viewModel.connectedDatabaseProperty.addListener { _, _, db ->
            when {
                db != null -> presentNavigation(db)
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

    override fun onMemoryDatabase() {
        viewModel.startMemoryDatabase()
    }

    @FXML fun onExit() {
        viewModel.exit()
    }

    override fun close() {
        activeController?.close()
        viewModel.close()
    }

    private fun presentWelcome() {

        FXMLLoader(WelcomeController.LAYOUT).load { node: Node, controller: WelcomeController ->
            controller.start(this)
            setContent(node, controller)
        }
    }

    private fun presentNavigation(database: ObservableMoneyDatabase) {

        FXMLLoader(NavigationController.LAYOUT).load { node: Node, controller: NavigationController ->
            controller.start(stage, database)
            setContent(node, controller)
        }
    }

    private fun setContent(content: Node, controller: AutoCloseable) {

        activeController?.close()
        activeController = controller

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
