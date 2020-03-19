package com.munzenberger.money.app.control

import com.munzenberger.money.app.property.AsyncObjectMapper
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsync
import javafx.collections.FXCollections
import javafx.collections.transformation.SortedList
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableView
import javafx.scene.paint.Color

inline fun <reified T> TableView<T>.bindAsync(listProperty: ReadOnlyAsyncObjectProperty<List<T>>, crossinline placeholder: () -> Node) {

    val transactionList = FXCollections.observableArrayList<T>().apply {
        bindAsync(listProperty)
    }

    val sortedList = SortedList(transactionList)

    // keep the table sorted when the contents change
    sortedList.comparatorProperty().bind(comparatorProperty())

    items = sortedList

    placeholderProperty().bindAsync(listProperty, object : AsyncObjectMapper<List<T>, Node> {

        override fun pending() = executing()

        override fun executing() = ProgressIndicator().apply {
            setPrefSize(60.0, 60.0)
            setMaxSize(60.0, 60.0)
        }

        override fun complete(obj: List<T>) = placeholder.invoke()

        override fun error(error: Throwable) = Label(error.message).apply {
            textFill = Color.RED
        }
    })
}
