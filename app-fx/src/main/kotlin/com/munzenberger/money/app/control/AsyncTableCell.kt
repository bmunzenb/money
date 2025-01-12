package com.munzenberger.money.app.control

import com.munzenberger.money.app.ErrorAlert
import com.munzenberger.money.app.property.AsyncObject
import javafx.scene.control.Hyperlink
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.paint.Color
import javafx.util.Callback

open class AsyncTableCell<S, T>(
    private val toString: (T) -> String = { it.toString() },
) : TableCell<S, AsyncObject<T>>() {
    override fun updateItem(
        item: AsyncObject<T>?,
        empty: Boolean,
    ) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            onEmpty()
        } else {
            when (item) {
                is AsyncObject.Pending<T> -> onPending()
                is AsyncObject.Executing<T> -> onExecuting()
                is AsyncObject.Complete<T> -> onComplete(item.value)
                is AsyncObject.Error<T> -> onError(item.error)
            }
        }
    }

    open fun onEmpty() {
        text = null
        graphic = null
    }

    open fun onPending() {
        onExecuting()
    }

    open fun onExecuting() {
        text = null
        graphic =
            ProgressIndicator().apply {
                setPrefSize(12.0, 12.0)
                setMaxSize(12.0, 12.0)
            }
    }

    open fun onComplete(value: T) {
        text = toString.invoke(value)
        graphic = null
    }

    open fun onError(error: Throwable) {
        text = null
        graphic =
            Hyperlink(error.message).apply {
                textFill = Color.RED
                setOnAction { ErrorAlert(error).showAndWait() }
            }
    }
}

class AsyncTableCellFactory<S, T>(
    private val toString: (T) -> String = { it.toString() },
) : Callback<TableColumn<S, AsyncObject<T>>, TableCell<S, AsyncObject<T>>> {
    override fun call(param: TableColumn<S, AsyncObject<T>>?) = AsyncTableCell<S, T>(toString)
}
