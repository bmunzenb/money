package com.munzenberger.money.app

import com.munzenberger.money.app.navigation.LayoutControllerNavigation
import com.munzenberger.money.core.MoneyDatabase
import javafx.stage.Stage
import java.net.URL

class AccountRegisterController : AutoCloseable {

    companion object {
        private val LAYOUT: URL = AccountListController::class.java.getResource("AccountRegisterLayout.fxml")

        fun navigation(stage: Stage, database: MoneyDatabase, accountIdentity: Long) = LayoutControllerNavigation(LAYOUT) {
            controller: AccountRegisterController -> controller.start(stage, database, accountIdentity)
        }
    }

    private lateinit var stage: Stage
    private lateinit var database: MoneyDatabase

    private val viewModel = AccountRegisterViewModel()

    fun initialize() {

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
