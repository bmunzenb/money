package com.munzenberger.money.app.property

import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableList

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

fun BooleanProperty.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<*>, vararg status: AsyncObject.Status) {

    val listener = { obj: AsyncObject<*> ->
        set(obj.status in status)
    }

    asyncObjectProperty.addListener { _, _, obj -> listener.invoke(obj) }

    listener.invoke(asyncObjectProperty.value)
}
