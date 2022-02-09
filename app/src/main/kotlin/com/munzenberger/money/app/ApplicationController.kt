package com.munzenberger.money.app

import com.munzenberger.money.app.control.booleanToWaitCursor
import com.munzenberger.money.app.database.FileDatabaseConnector
import com.munzenberger.money.app.database.MemoryDatabaseCallbacks
import com.munzenberger.money.app.database.NewFileDatabaseCallbacks
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.database.OpenFileDatabaseCallbacks
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.MenuBar
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.net.URL

class ApplicationController : DatabaseConnectorDelegate, AutoCloseable {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("ApplicationLayout.fxml")!!
    }

    @FXML lateinit var menuBar: MenuBar
    @FXML lateinit var contentPane: AnchorPane

    private val viewModel = ApplicationViewModel()
    private var activeController: AutoCloseable? = null

    // TODO remove reference to stage in controller
    private lateinit var stage: Stage

    @FXML fun initialize() {

        menuBar.isUseSystemMenuBar = true

        viewModel.connectedDatabaseProperty.addListener { _, _, db ->
            when {
                db != null -> presentNavigation(db)
                else -> presentWelcome()
            }
        }
    }

    fun start(stage: Stage) {
        this.stage = stage

        stage.titleProperty().bind(viewModel.titleProperty)

        viewModel.isConnectionInProgressProperty.addListener { _, _, newValue ->
            stage.scene.cursor = booleanToWaitCursor(newValue)
            stage.scene.root.isDisable = newValue
        }

        presentWelcome()
    }

    @FXML override fun onCreateDatabase() {
        createDatabaseFile()?.let {
            viewModel.openFileDatabase(it, NewFileDatabaseCallbacks)
        }
    }

    @FXML override fun onOpenDatabase() {
        openDatabaseFile()?.let {
            viewModel.openFileDatabase(it, OpenFileDatabaseCallbacks)
        }
    }

    @FXML override fun onMemoryDatabase() {
        viewModel.startMemoryDatabase(MemoryDatabaseCallbacks)
    }

    @FXML fun onExit() {
        Platform.exit()
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

        contentPane.children.apply {
            clear()
            add(content)
        }

        AnchorPane.setTopAnchor(content, 0.0)
        AnchorPane.setLeftAnchor(content, 0.0)
        AnchorPane.setRightAnchor(content, 0.0)
        AnchorPane.setBottomAnchor(content, 0.0)
    }

    private fun createDatabaseFile(): File? {
        val file: File? = FileChooser().let {
            it.title = "New Money Database"
            it.initialDirectory = File(System.getProperty("user.home"))
            it.initialFileName = "Money${FileDatabaseConnector.SUFFIX}"
            it.extensionFilters.addAll(
                    FileChooser.ExtensionFilter("Money Database Files", "*${FileDatabaseConnector.SUFFIX}"),
                    FileChooser.ExtensionFilter("All Files", "*"))
            it.showSaveDialog(stage)
        }

        file?.run {
            if (exists()) {
                if (!delete()) {

                    Alert(Alert.AlertType.ERROR).apply {
                        title = "Error"
                        contentText = "Could not delete existing file."
                        showAndWait()
                    }

                    return null
                }
            }
        }

        return file
    }

    private fun openDatabaseFile(): File? = FileChooser().let {
        it.title = "Open Money Database"
        it.initialDirectory = File(System.getProperty("user.home"))
        it.extensionFilters.addAll(
                FileChooser.ExtensionFilter("Money Database Files", "*${FileDatabaseConnector.SUFFIX}"),
                FileChooser.ExtensionFilter("All Files", "*"))
        it.showOpenDialog(stage)
    }
}
