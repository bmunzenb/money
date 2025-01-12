package com.munzenberger.money.app

import javafx.fxml.FXML
import java.net.URL

class WelcomeController : AutoCloseable {
    companion object {
        val LAYOUT: URL = ApplicationController::class.java.getResource("WelcomeLayout.fxml")
    }

    private val viewModel = WelcomeViewModel()

    private lateinit var databaseConnectorDelegate: DatabaseConnectorDelegate

    fun start(databaseConnectorDelegate: DatabaseConnectorDelegate) {
        this.databaseConnectorDelegate = databaseConnectorDelegate
    }

    @FXML fun onCreateDatabase() {
        databaseConnectorDelegate.onCreateDatabase()
    }

    @FXML fun onOpenDatabase() {
        databaseConnectorDelegate.onOpenDatabase()
    }

    @FXML fun onMemoryDatabase() {
        databaseConnectorDelegate.onMemoryDatabase()
    }

    override fun close() {
        // nothing to close
    }
}
