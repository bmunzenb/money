package com.munzenberger.money.desktop.accounts

/** The state of data a screen loads from the repository, such as the options for a dropdown. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data object Error : LoadState<Nothing>
    data class Loaded<T>(val value: T) : LoadState<T>
}

/** The loaded list, or an empty list while loading or after an error. */
val <T> LoadState<List<T>>.loadedOrEmpty: List<T>
    get() = (this as? LoadState.Loaded)?.value.orEmpty()

fun <T> Result<T>.toLoadState(): LoadState<T> =
    fold(onSuccess = { LoadState.Loaded(it) }, onFailure = { LoadState.Error })
