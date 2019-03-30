package com.munzenberger.money.app

import com.munzenberger.money.app.navigation.LayoutControllerNavigation
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.stage.Stage
import java.net.URL

class AccountRegisterController : AutoCloseable {

    companion object {
        private val LAYOUT: URL = AccountListController::class.java.getResource("AccountRegisterLayout.fxml")

        fun navigation(stage: Stage, database: MoneyDatabase, accountIdentity: Long) = LayoutControllerNavigation(LAYOUT) {
            controller: AccountRegisterController -> controller.start(stage, database, accountIdentity)
        }
    }

    @FXML lateinit var accountNameProgress: ProgressIndicator
    @FXML lateinit var accountNameLabel: Label

    private lateinit var stage: Stage
    private lateinit var database: MoneyDatabase

    private val viewModel = AccountRegisterViewModel()
    private val retainListeners = mutableListOf<ChangeListener<*>>()

    fun initialize() {

        retainListeners += accountNameProgress.visibleProperty().bindAsyncStatus(viewModel.accountProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING)

        accountNameLabel.apply {
            retainListeners += visibleProperty().bindAsyncStatus(viewModel.accountProperty, AsyncObject.Status.COMPLETE)
            retainListeners += textProperty().bindAsync(viewModel.accountProperty) { "Account Register : ${it.name}" }
            // TODO: what to display if there's an error?
        }
    }

    fun start(stage: Stage, database: MoneyDatabase, accountIdentity: Long) {
        this.stage = stage
        this.database = database

        viewModel.start(database, accountIdentity)
    }

    override fun close() {
        viewModel.close()
    }
}
