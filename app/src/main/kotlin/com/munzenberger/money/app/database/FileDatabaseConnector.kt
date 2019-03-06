package com.munzenberger.money.app.database

import java.io.File

class FileDatabaseConnector {

    companion object {
        const val DRIVER = "org.h2.Driver"
        const val SUFFIX = org.h2.engine.Constants.SUFFIX_MV_FILE
    }

    fun connect(file: File, callback: DatabaseConnector.Callback) {

        var name = file.absolutePath
        if (name.endsWith(SUFFIX)) {
           name = name.substring(0, name.length - SUFFIX.length)
        }

        val connectionUrl = "jdbc:h2:file:$name"

        DatabaseConnector().connect(driver = DRIVER, connectionUrl = connectionUrl, callback = callback)
    }
}
