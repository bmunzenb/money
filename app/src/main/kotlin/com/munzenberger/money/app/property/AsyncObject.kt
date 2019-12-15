package com.munzenberger.money.app.property

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
