package com.munzenberger.money.app.property

import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import java.util.concurrent.Callable

fun <T, R> Property<R>.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>, block: (AsyncObject<T>) -> R) {

    val callable: Callable<R> = Callable {
        block.invoke(asyncObjectProperty.value)
    }

    val binding = Bindings.createObjectBinding(callable, asyncObjectProperty)

    bind(binding)
}

fun <T, R> Property<R>.bindAsyncValue(asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>, block: (T) -> R) {

    val callable: Callable<R> = Callable {
        when (val async = asyncObjectProperty.value) {
            is AsyncObject.Complete -> block.invoke(async.value)
            else -> null
        }
    }

    val binding = Bindings.createObjectBinding(callable, asyncObjectProperty)

    bind(binding)
}

fun <T> ObservableList<T>.bindAsync(asyncObjectProperty: ReadOnlyAsyncObjectProperty<List<T>>) {

    val callable = { obj: AsyncObject<List<T>> -> when (obj) {
        is AsyncObject.Pending -> clear()
        is AsyncObject.Executing -> clear()
        is AsyncObject.Complete -> setAll(obj.value)
        is AsyncObject.Error -> clear()
    }}

    callable.invoke(asyncObjectProperty.value)

    val listener = ChangeListener { _, _, obj: AsyncObject<List<T>> -> callable.invoke(obj) }

    asyncObjectProperty.addListener(listener)
}

fun BooleanProperty.bindAsyncStatus(asyncObjectProperty: ReadOnlyAsyncObjectProperty<*>, vararg status: AsyncObject.Status) {

    val callable: Callable<Boolean> = Callable {
        asyncObjectProperty.value.status in status
    }

    val binding = Bindings.createBooleanBinding(callable, asyncObjectProperty)

    bind(binding)
}
