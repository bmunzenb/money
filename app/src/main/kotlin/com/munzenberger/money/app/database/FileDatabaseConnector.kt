package com.munzenberger.money.app.database

import com.munzenberger.money.core.SQLiteDatabaseDialect
import java.io.File

abstract class FileDatabaseConnector : DatabaseConnector() {

    companion object {
        const val SUFFIX = ".money"
    }

    abstract fun openFile(): File?

    fun connect(complete: DatabaseConnectionHandler) {

        when (val f = openFile()) {
            null -> complete.invoke(null)
            else -> connect(f, complete)
        }
    }

    private fun connect(file: File, complete: DatabaseConnectionHandler) {

        val name = file.absolutePath
        val connectionUrl = "jdbc:sqlite:$name"

        connect(name = name,
                driver = "org.sqlite.JDBC",
                dialect = SQLiteDatabaseDialect,
                connectionUrl = connectionUrl,
                complete = complete)

    }
}
