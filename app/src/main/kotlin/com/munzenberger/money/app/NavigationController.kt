package com.munzenberger.money.app

import javafx.fxml.FXML
import javafx.scene.control.Button
import java.net.URL

class NavigationController {

    companion object {
        val LAYOUT: URL = NavigationController::class.java.getResource("NavigationLayout.fxml")
    }

    @FXML lateinit var backButton: Button
    @FXML lateinit var forwardButton: Button

    private val viewModel = NavigationViewModel()

    fun initialize() {

    }

    fun start() {

    }

    @FXML fun onBackButton() {

    }

    @FXML fun onForwardButton() {

    }

    @FXML fun onAccountsButton() {

    }

    @FXML fun onQueryButton() {

    }
}