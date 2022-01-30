package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.navigation.LayoutControllerNavigation
import com.munzenberger.money.app.navigation.Navigator
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.net.URL

class NavigationController : AutoCloseable {

    companion object {
        val LAYOUT: URL = NavigationController::class.java.getResource("NavigationLayout.fxml")
    }

    @FXML lateinit var contentPane: AnchorPane
    @FXML lateinit var backButton: Button
    @FXML lateinit var forwardButton: Button

    private val navigator = Navigator {

        contentPane.children.apply {
            clear()
            add(it)
        }

        AnchorPane.setTopAnchor(it, 0.0)
        AnchorPane.setLeftAnchor(it, 0.0)
        AnchorPane.setRightAnchor(it, 0.0)
        AnchorPane.setBottomAnchor(it, 0.0)
    }

    private val accountsNavigation = LayoutControllerNavigation(AccountListController.LAYOUT) {
        controller: AccountListController -> controller.start(stage, database, navigator)
    }

    private val categoriesNavigation = LayoutControllerNavigation(CategoryListController.LAYOUT) {
        controller: CategoryListController -> controller.start(database)
    }

    private val payeesNavigation = LayoutControllerNavigation(PayeeListController.LAYOUT) {
        controller: PayeeListController -> controller.start(database)
    }

    private val queryNavigation = LayoutControllerNavigation(QueryController.LAYOUT) {
        controller: QueryController -> controller.start(database)
    }

    // TODO remove reference to stage
    private lateinit var stage: Stage
    private lateinit var database: ObservableMoneyDatabase

    fun initialize() {

        backButton.disableProperty().bind(navigator.backHistoryProperty.emptyProperty())
        forwardButton.disableProperty().bind(navigator.forwardHistoryProperty.emptyProperty())
    }

    fun start(stage: Stage, database: ObservableMoneyDatabase) {
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

    @FXML fun onCategoriesButton() {
        navigator.goTo(categoriesNavigation)
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
