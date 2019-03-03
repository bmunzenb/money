package com.munzenberger.money.app

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.MenuBar
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger

class ApplicationController {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("ApplicationLayout.fxml")
    }

    private val logger = Logger.getLogger(ApplicationController::class.java.simpleName)

    @FXML lateinit var menuBar: MenuBar
    @FXML lateinit var borderPane: BorderPane

    lateinit var stage: Stage

    fun initialize() {

    }

    @FXML fun onClose() {
        Platform.exit()
    }

    fun onApplicationClose() {
        logger.log(Level.INFO, "closing application")
    }
}
