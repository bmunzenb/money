package com.munzenberger.money.app.concurrent

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.AsyncObjectProperty
import com.munzenberger.money.app.property.AsyncStatusProperty
import javafx.concurrent.Task
import java.util.concurrent.Executor

fun <T> AsyncObjectProperty<T>.setValueAsync(executor: Executor = Executors.SINGLE, block: () -> T) {

    val task = object : Task<T>() {

        override fun call(): T {
            return block.invoke()
        }

        override fun running() {
            setValue(AsyncObject.Executing())
        }

        override fun succeeded() {
            setValue(AsyncObject.Complete(value))
        }

        override fun failed() {
            setValue(AsyncObject.Error(exception))
        }
    }

    executor.execute(task)
}

fun AsyncStatusProperty.executeAsync(executor: Executor = Executors.SINGLE, block: () -> Unit) {
    setValueAsync(executor, block)
}
