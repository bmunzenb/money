package com.munzenberger.money.repository.api

/**
 * Represents the state of an asynchronous data load operation.
 *
 * @param T the type of data being loaded
 */
sealed class ModelState<T> {

    /** Indicates that the data is currently being loaded. */
    class Loading<T> : ModelState<T>()

    /**
     * Indicates that the data was loaded successfully.
     *
     * @property data the loaded data
     */
    data class Success<T>(val data: T) : ModelState<T>()

    /**
     * Indicates that the data load failed.
     *
     * @property cause the exception that caused the failure
     */
    data class Error<T>(val cause: Throwable) : ModelState<T>()
}