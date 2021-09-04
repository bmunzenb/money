package com.munzenberger.money.app.property

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.Cursor

fun <T, R> ObservableValue<T>.map(block: (T) -> R): ObservableValue<R> {

    val property = SimpleObjectProperty<R>()
    val binding = Bindings.createObjectBinding({ block.invoke(value) }, this)

    return property.apply { bind(binding) }
}

fun booleanToCursor(input: Boolean): Cursor =
        when (input) {
            true -> Cursor.WAIT
            else -> Cursor.DEFAULT
        }
