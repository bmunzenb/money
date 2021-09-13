package com.munzenberger.money.app.concurrent

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.AsyncObjectProperty
import javafx.concurrent.Task
import java.util.concurrent.Executor

fun <T> AsyncObjectProperty<T>.setValueAsync(executor: Executor = Executors.SINGLE, block: () -> T) {

    val task = object : Task<T>() {

        override fun call(): T {
            return block.invoke()
        }

        override fun succeeded() {
            setValue(AsyncObject.Complete(value))
        }

        override fun failed() {
            setValue(AsyncObject.Error(exception))
        }
    }

    value = AsyncObject.Executing()

    executor.execute(task)
}
