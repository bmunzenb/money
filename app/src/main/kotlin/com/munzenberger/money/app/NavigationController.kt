package com.munzenberger.money.app

import com.munzenberger.money.app.navigation.Navigation
import com.munzenberger.money.app.navigation.Navigator
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import java.net.URL

class NavigationController {

    companion object {
        val LAYOUT: URL = NavigationController::class.java.getResource("NavigationLayout.fxml")
    }

    @FXML lateinit var borderPane: BorderPane
    @FXML lateinit var backButton: Button
    @FXML lateinit var forwardButton: Button

    private val navigator = Navigator { borderPane.center = it }

    private val accountsNavigation = Navigation(AccountsController.LAYOUT) { controller: AccountsController -> controller.start() }

    fun initialize() {

        backButton.disableProperty().bind(navigator.backHistoryProperty.emptyProperty())
        forwardButton.disableProperty().bind(navigator.forwardHistoryProperty.emptyProperty())
    }

    fun start() {
        navigator.goTo(accountsNavigation)
    }

    @FXML fun onBackButton() {
        navigator.goBack()
    }

    @FXML fun onForwardButton() {
        navigator.goForward()
    }

    @FXML fun onAccountsButton() {
        navigator.goTo(accountsNavigation)
    }

    @FXML fun onQueryButton() {

    }
}
