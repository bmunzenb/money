package com.munzenberger.money.app.control

import javafx.collections.ObservableList
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter

fun <T> autoCompleteTextFormatter(items: ObservableList<T>, converter: StringConverter<T>, defaultValue: T? = null) =
        TextFormatter(converter, defaultValue, AutoCompleteOperator(items, converter))
