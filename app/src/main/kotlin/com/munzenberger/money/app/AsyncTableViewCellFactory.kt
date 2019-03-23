package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.paint.Color
import javafx.util.Callback

class AsyncTableViewCellFactory<S, T>(private val onItem: (TableCell<S, AsyncObject<T>>, T?) -> Unit) : Callback<TableColumn<S, AsyncObject<T>>, TableCell<S, AsyncObject<T>>> {

    companion object {
        fun <S, T> text(block: (T?) -> String?) = AsyncTableViewCellFactory<S, T> { tableCell, item ->
            tableCell.text = block.invoke(item)
            tableCell.graphic = null
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
                is AsyncObject.Complete -> onItem.invoke(this, item.value)
            }
        }
    }

    private fun onEmpty(cell: TableCell<S, AsyncObject<T>>) {
        cell.text = null
        cell.graphic = null
    }

    private fun onExecuting(cell: TableCell<S, AsyncObject<T>>) {
        cell.text = null
        cell.graphic = ProgressIndicator().apply {
            setPrefSize(12.0, 12.0)
            setMaxSize(12.0, 12.0)
        }
    }

    private fun onError(cell: TableCell<S, AsyncObject<T>>, error: Throwable) {
        cell.text = null
        cell.graphic = Label(error.message).apply {
            textFill = Color.RED
        }
    }
}
