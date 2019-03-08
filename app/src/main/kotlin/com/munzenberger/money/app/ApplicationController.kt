package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.scene.control.MenuBar
import javafx.scene.control.ToolBar
import javafx.stage.Stage
import java.net.URL

class ApplicationController {

    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("ApplicationLayout.fxml")
    }

    @FXML lateinit var menuBar: MenuBar
    @FXML lateinit var toolBar: ToolBar

    private lateinit var stage: Stage

    fun start(stage: Stage) {
        this.stage = stage
    }
}
