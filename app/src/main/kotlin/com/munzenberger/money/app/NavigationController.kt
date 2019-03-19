package com.munzenberger.money.app

import com.munzenberger.money.app.navigation.Navigation
import com.munzenberger.money.app.navigation.Navigator
import com.munzenberger.money.core.MoneyDatabase
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.net.URL

class NavigationController {

    companion object {
        val LAYOUT: URL = NavigationController::class.java.getResource("NavigationLayout.fxml")
    }

    @FXML lateinit var borderPane: BorderPane
    @FXML lateinit var backButton: Button
    @FXML lateinit var forwardButton: Button

    private val navigator = Navigator { borderPane.center = it }

    private val accountsNavigation = Navigation(AccountListController.LAYOUT) {
        controller: AccountListController -> controller.start(stage, database)
    }

    private val queryNavigation = Navigation(QueryController.LAYOUT) {
        controller: QueryController -> controller.start(database)
    }

    private lateinit var stage: Stage
    private lateinit var database: MoneyDatabase

    fun initialize() {

        backButton.disableProperty().bind(navigator.backHistoryProperty.emptyProperty())
        forwardButton.disableProperty().bind(navigator.forwardHistoryProperty.emptyProperty())
    }

    fun start(stage: Stage, database: MoneyDatabase) {
        this.stage = stage
        this.database = database

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
        navigator.goTo(queryNavigation)
    }
}
