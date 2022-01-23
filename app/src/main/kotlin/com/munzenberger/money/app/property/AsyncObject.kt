package com.munzenberger.money.app.property

import com.munzenberger.money.core.Money

sealed class AsyncObject<T>(val status: Status) : Comparable<AsyncObject<T>> {

    enum class Status {
        PENDING, EXECUTING, COMPLETE, ERROR
    }

    class Pending<T> : AsyncObject<T>(Status.PENDING)
    class Executing<T> : AsyncObject<T>(Status.EXECUTING)
    class Complete<T>(val value: T) : AsyncObject<T>(Status.COMPLETE)
    class Error<T>(val error: Throwable) : AsyncObject<T>(Status.ERROR)

    override fun compareTo(other: AsyncObject<T>) = status.compareTo(other.status)
}

class AsyncObjectComparator<T : Comparable<T>> : Comparator<AsyncObject<T>> {

    override fun compare(o1: AsyncObject<T>?, o2: AsyncObject<T>?): Int {
        return when {
            o1 == null -> -1
            o2 == null -> 1
            o1 is AsyncObject.Complete<T> && o2 is AsyncObject.Complete<T> -> o1.value.compareTo(o2.value)
            else -> o1.compareTo(o2)
        }
    }
}

fun <T, R> AsyncObject<T>.map(block: (T) -> R): AsyncObject<R> =
    when (this) {
        is AsyncObject.Pending -> AsyncObject.Pending()
        is AsyncObject.Executing -> AsyncObject.Executing()
        is AsyncObject.Complete -> AsyncObject.Complete(block.invoke(value))
        is AsyncObject.Error -> AsyncObject.Error(error)
    }

fun <T> AsyncObject<T>.combine(other: AsyncObject<T>, combiner: (value1: T, value2: T) -> T): AsyncObject<T> {

    if (this is AsyncObject.Complete && other is AsyncObject.Complete) {
        return AsyncObject.Complete(combiner.invoke(this.value, other.value))
    }

    if (this is AsyncObject.Error) {
        return AsyncObject.Error(this.error)
    }

    if (other is AsyncObject.Error) {
        return AsyncObject.Error(other.error)
    }

    if (this is AsyncObject.Executing || other is AsyncObject.Executing) {
        return AsyncObject.Executing()
    }

    return AsyncObject.Pending()
}
