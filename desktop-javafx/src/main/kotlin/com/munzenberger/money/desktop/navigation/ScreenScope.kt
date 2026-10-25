package com.munzenberger.money.desktop.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.ParametersDefinition
import kotlin.reflect.KClass

/**
 * Owns the ViewModels and coroutines of one back stack entry. [close] clears its ViewModels and
 * cancels its collectors when the entry is popped.
 */
class ScreenScope : ViewModelStoreOwner, KoinComponent, AutoCloseable {
    override val viewModelStore = ViewModelStore()

    // Runs on the JavaFX application thread, so collectors can update nodes directly.
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Returns this entry's ViewModel of type [T], creating it from Koin the first time. */
    inline fun <reified T : ViewModel> viewModel(noinline parameters: ParametersDefinition? = null): T =
        viewModel(T::class, parameters)

    fun <T : ViewModel> viewModel(modelClass: KClass<T>, parameters: ParametersDefinition? = null): T =
        ViewModelProvider.create(viewModelStore, KoinViewModelFactory(getKoin(), parameters))[modelClass]

    /** Calls [action] on the JavaFX application thread with each value of [flow] until this scope closes. */
    fun <T> collect(flow: Flow<T>, action: (T) -> Unit) {
        coroutineScope.launch { flow.collect(action) }
    }

    override fun close() {
        coroutineScope.cancel()
        viewModelStore.clear()
    }
}

private class KoinViewModelFactory(
    private val koin: Koin,
    private val parameters: ParametersDefinition?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
        koin.get(modelClass, parameters = parameters)
}
