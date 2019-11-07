package com.munzenberger.money.app.control

import javafx.util.StringConverter

class BlockStringConverter<T>(
        private val toString: (T) -> String?,
        private val toObject: (String) -> T
) : StringConverter<T>() {

    override fun toString(obj: T?): String =
            obj?.let { toString.invoke(it) } ?: ""

    override fun fromString(string: String?): T? =
            when {
                string.isNullOrBlank() -> null
                else -> toObject.invoke(string)
            }
}

class ListLookupStringConverter<T>(
        private val list: List<T>,
        private val converter: StringConverter<T>
) : StringConverter<T>() {

    constructor(list: List<T>, toString: (T) -> String?, toObject: (String) -> T) :
            this(list, BlockStringConverter(toString, toObject))

    override fun toString(obj: T?): String =
            converter.toString(obj)

    override fun fromString(string: String?): T? =
            when {
                string.isNullOrBlank() -> null
                else -> list.find { converter.toString(it) == string } ?: converter.fromString(string)
            }
}
