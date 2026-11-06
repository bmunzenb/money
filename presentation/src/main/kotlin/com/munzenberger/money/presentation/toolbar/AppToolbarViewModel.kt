package com.munzenberger.money.presentation.toolbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppToolbarViewModel(
    private val navigator: Navigator
) : ViewModel() {

    val state: StateFlow<AppToolbarUiState> = navigator.backStack
        .map(::uiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = uiState(navigator.backStack.value),
        )

    fun onBackClick() {
        navigator.back()
    }

    fun onNewTransactionClick() {
        if (!state.value.canAddTransaction) return
        navigator.navigate { add(Route.NewTransaction()) }
    }

    private fun uiState(backStack: List<Route>) = AppToolbarUiState(
        canNavigateBack = backStack.size > 1,
        canAddTransaction = backStack.lastOrNull() !is Route.NewTransaction,
    )
}
