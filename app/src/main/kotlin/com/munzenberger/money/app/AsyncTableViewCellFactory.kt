package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.paint.Color
import javafx.util.Callback

abstract class AsyncTableViewCellFactory<S, T> : Callback<TableColumn<S, AsyncObject<T>>, TableCell<S, AsyncObject<T>>> {

    companion object {
        fun <S, T> text(block: (T) -> String? = { it.toString() }) = object : AsyncTableViewCellFactory<S, T>() {
            override fun onItem(cell: TableCell<S, AsyncObject<T>>, item: T) {
                cell.text = block.invoke(item)
                cell.graphic = null
            }
        }
    }

    override fun call(param: TableColumn<S, AsyncObject<T>>?) = object : TableCell<S, AsyncObject<T>>() {

        override fun updateItem(item: AsyncObject<T>?, empty: Boolean) {
            super.updateItem(item, empty)

            if (empty || item == null) onEmpty(this)
            else when (item) {
                is AsyncObject.Pending -> onExecuting(this)
                is AsyncObject.Executing -> onExecuting(this)
                is AsyncObject.Error -> onError(this, item.error)
                is AsyncObject.Complete -> onItem(this, item.value)
            }
        }
    }

    abstract fun onItem(cell: TableCell<S, AsyncObject<T>>, item: T)

    open fun onEmpty(cell: TableCell<S, AsyncObject<T>>) {
        cell.text = null
        cell.graphic = null
    }

    open fun onExecuting(cell: TableCell<S, AsyncObject<T>>) {
        cell.text = null
        cell.graphic = ProgressIndicator().apply {
            setPrefSize(12.0, 12.0)
            setMaxSize(12.0, 12.0)
        }
    }

    open fun onError(cell: TableCell<S, AsyncObject<T>>, error: Throwable) {
        cell.text = null
        cell.graphic = Label(error.message).apply {
            textFill = Color.RED
        }
    }
}
