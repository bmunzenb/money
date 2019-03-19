package com.munzenberger.money.app.property

sealed class AsyncObject<T>(val status: Status) {

    enum class Status {
        PENDING, EXECUTING, COMPLETE, ERROR
    }

    class Pending<T> : AsyncObject<T>(Status.PENDING)
    class Executing<T> : AsyncObject<T>(Status.EXECUTING)
    class Complete<T>(val value: T) : AsyncObject<T>(Status.COMPLETE)
    class Error<T>(val error: Throwable) : AsyncObject<T>(Status.ERROR)
}
