package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import java.net.URL

class CategoryListController : AutoCloseable {

    companion object {
        val LAYOUT: URL = PayeeListController::class.java.getResource("CategoryListLayout.fxml")
    }

    private val viewModel = CategoryListViewModel()

    fun start(database: ObservableMoneyDatabase) {

    }

    override fun close() {
        viewModel.close()
    }
}
