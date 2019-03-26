package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.navigation.LayoutControllerNavigation
import java.net.URL

class AccountRegisterController : AutoCloseable {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("AccountRegisterLayout.fxml")

        fun navigation(account: FXAccount) = LayoutControllerNavigation(LAYOUT) {
            controller: AccountRegisterController -> controller.start(account)
        }
    }

    private val viewModel = AccountRegisterViewModel()

    fun initialize() {

    }

    fun start(account: FXAccount) {

    }

    override fun close() {
        // nothing to close
    }
}
