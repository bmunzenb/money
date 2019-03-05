package com.munzenberger.money.app.database

import java.io.File

class FileDatabaseConnector {

    companion object {
        private const val DRIVER = "org.h2.Driver"
        private const val SUFFIX = ".h2.db"
    }

    fun connect(file: File, callback: DatabaseConnector.Callback) {

        var name = file.absolutePath
        if (name.endsWith(SUFFIX)) {
           name = name.substring(0, name.length - SUFFIX.length)
        }

        val connectionUrl = "jdbc:h2:file:$name;MV_STORE=FALSE;MVCC=FALSE"

        DatabaseConnector().connect(driver = DRIVER, connectionUrl = connectionUrl, callback = callback)
    }
}
