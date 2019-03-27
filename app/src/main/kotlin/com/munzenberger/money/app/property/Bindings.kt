package com.munzenberger.money.app.property

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import javafx.collections.ObservableList

interface AsyncObjectMapper<T, R> {
    fun pending(): R
    fun executing(): R
    fun complete(obj: T): R
    fun error(error: Throwable): R
}

fun <T, R> Property<R>.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>, mapper: AsyncObjectMapper<T, R>): ChangeListener<AsyncObject<T>> {

    val callable = { obj: AsyncObject<T> -> when (obj) {
        is AsyncObject.Pending -> setValue(mapper.pending())
        is AsyncObject.Executing -> setValue(mapper.executing())
        is AsyncObject.Complete -> setValue(mapper.complete(obj.value))
        is AsyncObject.Error -> setValue(mapper.error(obj.error))
    }}

    callable.invoke(asyncObjectProperty.value)

    val listener = ChangeListener { _, _, obj: AsyncObject<T> -> callable.invoke(obj) }

    asyncObjectProperty.addListener(WeakChangeListener(listener))

    return listener
}

fun <T> ObservableList<T>.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<List<T>>): ChangeListener<AsyncObject<List<T>>> {

    val callable = { obj: AsyncObject<List<T>> -> when (obj) {
        is AsyncObject.Pending -> clear()
        is AsyncObject.Executing -> clear()
        is AsyncObject.Complete -> setAll(obj.value)
        is AsyncObject.Error -> clear()
    }}

    callable.invoke(asyncObjectProperty.value)

    val listener = ChangeListener { _, _, obj: AsyncObject<List<T>> -> callable.invoke(obj) }

    asyncObjectProperty.addListener(WeakChangeListener(listener))

    return listener
}

fun BooleanProperty.bindAsyncStatus(asyncObjectProperty: ReadOnlyAsyncObjectProperty<*>, vararg status: AsyncObject.Status): ChangeListener<AsyncObject<*>> {

    val callable = { obj: AsyncObject<*> ->
        set(obj.status in status)
    }

    callable.invoke(asyncObjectProperty.value)

    val listener = ChangeListener { _, _, obj: AsyncObject<*> -> callable.invoke(obj) }

    asyncObjectProperty.addListener(WeakChangeListener(listener))

    return listener
}

fun <T> StringProperty.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>, toString: (T?) -> String? = { it?.toString() }) =
        bindAsync(asyncObjectProperty, object : AsyncObjectMapper<T, String?> {
            override fun pending(): String? = null
            override fun executing(): String? = null
            override fun complete(obj: T) = toString.invoke(obj)
            override fun error(error: Throwable): String? = null
        })

fun <T, R> AsyncObjectProperty<R>.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>, block: AsyncObjectProperty<R>.(T) -> Unit): ChangeListener<AsyncObject<T>> {

    val callable = { obj: AsyncObject<T> -> when (obj) {
        is AsyncObject.Pending -> setValue(AsyncObject.Pending())
        is AsyncObject.Executing -> setValue(AsyncObject.Executing())
        is AsyncObject.Error -> setValue(AsyncObject.Error(obj.error))
        is AsyncObject.Complete -> block.invoke(this, obj.value)
    }}

    callable.invoke(asyncObjectProperty.value)

    val listener = ChangeListener { _, _, obj: AsyncObject<T> -> callable.invoke(obj) }

    asyncObjectProperty.addListener(WeakChangeListener(listener))

    return listener
}
