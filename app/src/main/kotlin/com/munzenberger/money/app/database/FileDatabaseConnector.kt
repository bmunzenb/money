package com.munzenberger.money.app.database

import com.munzenberger.money.core.SQLiteDatabaseDialect
import java.io.File

class FileDatabaseConnector {

    companion object {
        const val SUFFIX = ".money"
    }

    fun connect(file: File, callback: DatabaseConnector.Callback) {

        val name = file.absolutePath

        val connectionUrl = "jdbc:sqlite:$name"

        DatabaseConnector().connect(
                name = name,
                driver = "org.sqlite.JDBC",
                dialect = SQLiteDatabaseDialect,
                connectionUrl = connectionUrl,
                callback = callback)
    }
}
