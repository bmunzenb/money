package com.munzenberger.money.app

import com.munzenberger.money.app.navigation.LayoutControllerNavigation
import com.munzenberger.money.app.navigation.Navigation
import com.munzenberger.money.app.navigation.Navigator
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.net.URL

class NavigationController : AutoCloseable {

    companion object {
        val LAYOUT: URL = NavigationController::class.java.getResource("NavigationLayout.fxml")
    }

    @FXML lateinit var borderPane: BorderPane
    @FXML lateinit var backButton: Button
    @FXML lateinit var forwardButton: Button

    private val navigator = Navigator { borderPane.center = it }

    private val accountsNavigation = LayoutControllerNavigation(AccountListController.LAYOUT) {
        controller: AccountListController -> controller.start(stage, database, navigator)
    }

    private val payeesNavigation = LayoutControllerNavigation(PayeeListController.LAYOUT) {
        controller: PayeeListController -> controller.start(database, schedulers)
    }

    private val queryNavigation = LayoutControllerNavigation(QueryController.LAYOUT) {
        controller: QueryController -> controller.start(database)
    }

    private lateinit var stage: Stage
    private lateinit var database: ObservableMoneyDatabase
    private lateinit var schedulers: SchedulerProvider

    fun initialize() {

        backButton.disableProperty().bind(navigator.backHistoryProperty.emptyProperty())
        forwardButton.disableProperty().bind(navigator.forwardHistoryProperty.emptyProperty())
    }

    fun start(stage: Stage, database: ObservableMoneyDatabase, schedulers: SchedulerProvider) {
        this.stage = stage
        this.database = database
        this.schedulers = schedulers

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

    @FXML fun onPayeesButton() {
        navigator.goTo(payeesNavigation)
    }

    @FXML fun onQueryButton() {
        navigator.goTo(queryNavigation)
    }

    override fun close() {
        navigator.close()
    }
}
