package com.munzenberger.money.core.demo

sealed interface PopulateDemoDataResult {
    data object Success : PopulateDemoDataResult

    /** The demo data couldn't be written, so none of it was added. */
    data class Failure(val cause: Throwable) : PopulateDemoDataResult
}
