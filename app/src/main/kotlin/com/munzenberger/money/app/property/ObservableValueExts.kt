package com.munzenberger.money.app.property

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue

fun <T, R> ObservableValue<T>.map(block: (T) -> R): ObservableValue<R> {
    val binding = Bindings.createObjectBinding({ block.invoke(value) }, this)
    return SimpleObjectProperty<R>().apply { bind(binding) }
}
