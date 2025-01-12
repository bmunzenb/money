package com.munzenberger.money.app.control

import javafx.util.StringConverter

class ListLookupStringConverter<T>(
    private val list: List<T>,
    private val converter: StringConverter<T>,
) : StringConverter<T>() {
    override fun toString(obj: T?): String = converter.toString(obj)

    override fun fromString(string: String?): T? =
        when {
            string.isNullOrBlank() -> null
            else -> list.find { converter.toString(it) == string } ?: converter.fromString(string)
        }
}
