package com.munzenberger.money.app

import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableObjectValue

object ApplicationState {

    private val databaseProperty = SimpleObjectProperty<MoneyDatabase?>()

    var database: MoneyDatabase?
        get() = databaseProperty.get()
        set(value) {
            databaseProperty.get()?.close()
            databaseProperty.set(value)
        }

    val observableDatabase: ObservableObjectValue<MoneyDatabase?> = databaseProperty
}
