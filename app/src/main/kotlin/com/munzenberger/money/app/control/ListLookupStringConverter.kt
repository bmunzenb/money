package com.munzenberger.money.app.control

import javafx.util.StringConverter

class ListLookupStringConverter<T>(
        private val list: List<T>,
        private val toString: (T) -> String?,
        private val toObject: (String) -> T
) : StringConverter<T>() {

    override fun toString(obj: T?): String =
            obj?.let { toString.invoke(it) } ?: ""

    override fun fromString(string: String?): T? =
            when {
                string.isNullOrBlank() -> null
                else -> list.find { toString.invoke(it) == string } ?: toObject.invoke(string)
            }
}
