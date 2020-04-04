package com.munzenberger.money.app.database

import com.munzenberger.money.core.SQLiteDatabaseDialect

class MemoryDatabaseConnector : DatabaseConnector() {

    override fun connect(callbacks: DatabaseConnectorCallbacks) {

        val driver = "org.sqlite.JDBC"
        val connectionUrl = "jdbc:sqlite::memory:"

        connect(name = connectionUrl,
                driver = driver,
                connectionUrl = connectionUrl,
                dialect = SQLiteDatabaseDialect,
                callbacks = callbacks)
    }
}
