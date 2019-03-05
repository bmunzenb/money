package com.munzenberger.money.app.database

import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File

class OpenFileDatabaseConnector(private val ownerWindow: Window) {

    fun connect(callback: DatabaseConnector.Callback) {

        val file: File? = FileChooser().apply {
            title = "Open Money Database"
            initialDirectory = File(System.getProperty("user.home"))
            extensionFilters.add(FileChooser.ExtensionFilter("Money Database Files", "*.h2.db"))
        }.showOpenDialog(ownerWindow)

        file?.run {
            FileDatabaseConnector().connect(this, callback)
        }
    }
}
