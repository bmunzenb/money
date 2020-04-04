package com.munzenberger.money.app.database

import com.munzenberger.money.app.ErrorAlert
import com.munzenberger.money.core.rx.ObservableMoneyDatabase

open class MemoryDatabase : DatabaseConnectorCallbacks {

    override fun onCanceled() {
        // Do nothing
    }

    override fun onConnected(database: ObservableMoneyDatabase) {
        // Do nothing
    }

    override fun onUnsupportedVersion() {
        // this should not happen when creating a memory database
        ErrorAlert.showAndWait(IllegalStateException("Received unsupported database error while starting memory database."))
    }

    override fun onPendingUpgrades() = true

    override fun onConnectError(error: Throwable) {
        ErrorAlert.showAndWait(error)
    }
}
