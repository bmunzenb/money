package com.munzenberger.money.app.control

import com.munzenberger.money.app.ErrorAlert
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.toObservableList
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.scene.Node
import javafx.scene.control.Hyperlink
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableView
import javafx.scene.paint.Color
import java.util.function.Predicate

inline fun <reified T> TableView<T>.bindAsync(
    listProperty: ReadOnlyAsyncObjectProperty<List<T>>,
    filterProperty: ReadOnlyObjectProperty<Predicate<T>> = SimpleObjectProperty(),
    placeholder: Node,
) {
    val observableList = listProperty.toObservableList()

    val filteredList =
        FilteredList(observableList).apply {
            predicateProperty().bind(filterProperty)
        }

    val sortedList = SortedList(filteredList)

    // keep the table sorted when the contents change
    sortedList.comparatorProperty().bind(comparatorProperty())

    items = sortedList

    placeholderProperty().bindAsync(listProperty) { async ->
        when (async) {
            is AsyncObject.Pending, is AsyncObject.Executing ->
                ProgressIndicator().apply {
                    setPrefSize(60.0, 60.0)
                    setMaxSize(60.0, 60.0)
                }

            is AsyncObject.Complete -> placeholder

            is AsyncObject.Error ->
                Hyperlink(async.error.message).apply {
                    textFill = Color.RED
                    setOnAction { ErrorAlert(async.error).showAndWait() }
                }
        }
    }
}
