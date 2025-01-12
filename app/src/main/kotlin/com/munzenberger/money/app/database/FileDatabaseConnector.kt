package com.munzenberger.money.app.database

import com.munzenberger.money.core.SQLiteDatabaseDialect
import java.io.File

class FileDatabaseConnector(private val file: File) : DatabaseConnector() {
    companion object {
        const val SUFFIX = ".money"
    }

    override fun connect(callbacks: DatabaseConnectorCallbacks) {
        val name = file.absolutePath
        val connectionUrl = "jdbc:sqlite:$name"

        connect(
            name = name,
            dialect = SQLiteDatabaseDialect,
            connectionUrl = connectionUrl,
            callbacks = callbacks,
        )
    }
}
