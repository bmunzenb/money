package com.munzenberger.money.app.property

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.util.concurrent.Callable

fun <T> ReadOnlyAsyncObjectProperty<List<T>>.toObservableList() : ObservableList<T> {

    val list = FXCollections.observableArrayList<T>()

    // update the items in the list if the value of the property changes
    val callable = { obj: AsyncObject<List<T>> -> when (obj) {
        //is AsyncObject.Pending -> list.clear()
        //is AsyncObject.Executing -> list.clear()
        is AsyncObject.Complete -> list.setAll(obj.value)
        is AsyncObject.Error -> list.clear()
        else -> Unit // do nothing
    }}

    callable.invoke(value)

    val listener = ChangeListener { _, _, obj: AsyncObject<List<T>> -> callable.invoke(obj) }

    addListener(listener)

    return FXCollections.unmodifiableObservableList(list)
}

fun <T, R> ReadOnlyObjectProperty<T>.toBinding(block: (T) -> R): ObjectBinding<R> {
    val callable = Callable { block(value) }
    return Bindings.createObjectBinding(callable, this)
}
