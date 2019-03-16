package com.munzenberger.money.app.database

import com.munzenberger.money.core.SQLiteDatabaseDialect
import javafx.stage.Window
import java.io.File

abstract class FileDatabaseConnector : DatabaseConnector() {

    companion object {
        const val SUFFIX = ".money"
    }

    fun connect(ownerWindow: Window, complete: DatabaseConnectionHandler) {
        openFile(ownerWindow)?.run { connect(this, complete) }
    }

    protected open fun connect(file: File, complete: DatabaseConnectionHandler) {

        val name = file.absolutePath
        val connectionUrl = "jdbc:sqlite:$name"

        connect(name = name,
                driver = "org.sqlite.JDBC",
                dialect = SQLiteDatabaseDialect,
                connectionUrl = connectionUrl,
                complete = complete)

    }

    protected abstract fun openFile(ownerWindow: Window): File?
}
