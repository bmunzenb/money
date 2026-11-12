package com.munzenberger.money.presentation.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** The state of data a screen loads from the repository, such as the options for a dropdown. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data object Error : LoadState<Nothing>
    data class Loaded<T>(val value: T) : LoadState<T>
}

/** The loaded value transformed by [transform], or the same loading or error state. */
fun <T, R> LoadState<T>.map(transform: (T) -> R): LoadState<R> = when (this) {
    LoadState.Loading -> LoadState.Loading
    LoadState.Error -> LoadState.Error
    is LoadState.Loaded -> LoadState.Loaded(transform(value))
}

/** The loaded value, or null while loading or after an error. */
val <T> LoadState<T>.loadedOrNull: T?
    get() = (this as? LoadState.Loaded)?.value

/** The loaded list, or an empty list while loading or after an error. */
val <T> LoadState<List<T>>.loadedOrEmpty: List<T>
    get() = loadedOrNull.orEmpty()

fun <T> Result<T>.toLoadState(): LoadState<T> =
    fold(onSuccess = { LoadState.Loaded(it) }, onFailure = { LoadState.Error })

/** Maps each result to its [LoadState], starting with [LoadState.Loading] until the first result arrives. */
fun <T> Flow<Result<T>>.toLoadStates(): Flow<LoadState<T>> =
    map { it.toLoadState() }.onStart { emit(LoadState.Loading) }
