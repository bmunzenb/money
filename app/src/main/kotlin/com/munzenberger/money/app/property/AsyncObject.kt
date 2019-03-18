package com.munzenberger.money.app.property

sealed class AsyncObject<T> {
    class Pending<T> : AsyncObject<T>()
    class Executing<T> : AsyncObject<T>()
    class Complete<T>(val value: T) : AsyncObject<T>()
    class Error<T>(val error: Throwable) : AsyncObject<T>()
}
