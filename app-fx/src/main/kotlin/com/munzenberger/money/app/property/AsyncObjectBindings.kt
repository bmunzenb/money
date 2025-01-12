package com.munzenberger.money.app.property

import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import java.util.concurrent.Callable

fun <T, R> Property<R>.bindAsync(
    asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>,
    block: (AsyncObject<T>) -> R,
) {
    val callable: Callable<R> =
        Callable {
            block.invoke(asyncObjectProperty.value)
        }

    val binding = Bindings.createObjectBinding(callable, asyncObjectProperty)

    bind(binding)
}

fun <T, R> Property<R>.bindAsyncValue(
    asyncObjectProperty: ReadOnlyAsyncObjectProperty<T>,
    block: (T) -> R,
) {
    val callable: Callable<R> =
        Callable {
            when (val async = asyncObjectProperty.value) {
                is AsyncObject.Complete -> block.invoke(async.value)
                else -> null
            }
        }

    val binding = Bindings.createObjectBinding(callable, asyncObjectProperty)

    bind(binding)
}

fun BooleanProperty.bindAsyncStatus(
    asyncObjectProperty: ReadOnlyAsyncObjectProperty<*>,
    vararg status: AsyncObject.Status,
) {
    val callable: Callable<Boolean> =
        Callable {
            asyncObjectProperty.value.status in status
        }

    val binding = Bindings.createBooleanBinding(callable, asyncObjectProperty)

    bind(binding)
}
