package com.munzenberger.money.app.database

import com.munzenberger.money.core.SQLiteDatabaseDialect

class MemoryDatabaseConnector : DatabaseConnector() {
    override fun connect(callbacks: DatabaseConnectorCallbacks) {
        val connectionUrl = "jdbc:sqlite::memory:"

        connect(
            name = connectionUrl,
            connectionUrl = connectionUrl,
            dialect = SQLiteDatabaseDialect,
            callbacks = callbacks,
        )
    }
}
