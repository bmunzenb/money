package com.munzenberger.money.app.property

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList

interface AsyncObjectMapper<T, R> {
    fun pending(): R
    fun executing(): R
    fun complete(obj: T): R
    fun error(error: Throwable): R
}

fun <T, R> ObjectProperty<R>.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>, mapper: AsyncObjectMapper<T, R>) {

    val listener = { obj: AsyncObject<T> -> when (obj) {
        is AsyncObject.Pending -> set(mapper.pending())
        is AsyncObject.Executing -> set(mapper.executing())
        is AsyncObject.Complete -> set(mapper.complete(obj.value))
        is AsyncObject.Error -> set(mapper.error(obj.error))
    }}

    asyncObjectProperty.addListener { _, _, obj -> listener.invoke(obj) }

    listener.invoke(asyncObjectProperty.value)
}

fun <T> ObservableList<T>.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<List<T>>) {

    val listener = { obj: AsyncObject<List<T>> -> when (obj) {
        is AsyncObject.Pending -> clear()
        is AsyncObject.Executing -> clear()
        is AsyncObject.Complete -> setAll(obj.value)
        is AsyncObject.Error -> clear()
    }}

    asyncObjectProperty.addListener { _, _, obj -> listener.invoke(obj) }

    listener.invoke(asyncObjectProperty.value)
}

fun BooleanProperty.bindAsyncStatus(asyncObjectProperty: ReadOnlyAsyncObjectProperty<*>, vararg status: AsyncObject.Status) {

    val listener = { obj: AsyncObject<*> ->
        set(obj.status in status)
    }

    asyncObjectProperty.addListener { _, _, obj -> listener.invoke(obj) }

    listener.invoke(asyncObjectProperty.value)
}

fun <T> StringProperty.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>, toString: (T?) -> String? = { it?.toString() }) {

    val listener = { obj: AsyncObject<T> -> when (obj) {
        is AsyncObject.Complete -> set(toString.invoke(obj.value))
        else -> set(null)
    }}

    asyncObjectProperty.addListener { _, _, obj -> listener.invoke(obj) }

    listener.invoke(asyncObjectProperty.value)
}

fun <T, R> AsyncObjectProperty<R>.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>, block: AsyncObjectProperty<R>.(T) -> Unit) {

    val listener = { obj: AsyncObject<T> -> when (obj) {
        is AsyncObject.Pending -> setValue(AsyncObject.Pending())
        is AsyncObject.Executing -> setValue(AsyncObject.Executing())
        is AsyncObject.Error -> setValue(AsyncObject.Error(obj.error))
        is AsyncObject.Complete -> block.invoke(this, obj.value)
    }}

    asyncObjectProperty.addListener { _, _, obj -> listener.invoke(obj) }

    listener.invoke(asyncObjectProperty.value)
}
