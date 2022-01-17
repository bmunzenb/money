package com.munzenberger.money.app.property

import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList

fun <T> ReadOnlyAsyncObjectProperty<List<T>>.toObservableList() : ObservableList<T> {

    return FXCollections.observableArrayList<T>().apply {

        // update the items in the list if the value of the property changes
        val callable = { obj: AsyncObject<List<T>> -> when (obj) {
            is AsyncObject.Pending -> clear()
            is AsyncObject.Executing -> clear()
            is AsyncObject.Complete -> setAll(obj.value)
            is AsyncObject.Error -> clear()
        }}

        callable.invoke(value)

        val listener = ChangeListener { _, _, obj: AsyncObject<List<T>> -> callable.invoke(obj) }

        addListener(listener)
    }
}
