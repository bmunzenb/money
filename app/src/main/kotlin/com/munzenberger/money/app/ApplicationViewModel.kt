package com.munzenberger.money.app

import javafx.application.Platform
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

class ApplicationViewModel : DatabaseConnectorViewModel {

    companion object {
        const val DEFAULT_TITLE = "Money"
    }

    private val title = SimpleStringProperty(DEFAULT_TITLE)

    val titleProperty: ReadOnlyStringProperty = title

    init {
        MoneyApplication.observableDatabase.addListener { _, _, db ->
            title.value = db?.name ?: DEFAULT_TITLE
        }
    }

    fun exit() {
        Platform.exit()
    }
}
