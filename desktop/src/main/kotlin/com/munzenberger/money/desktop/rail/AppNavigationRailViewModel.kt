package com.munzenberger.money.desktop.rail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.desktop.navigation.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppNavigationRailViewModel(
    private val navigator: Navigator
) : ViewModel() {

    val state: StateFlow<AppNavigationRailUiState> = navigator.currentRoute
        .map { route -> AppNavigationRailUiState(selected = TopLevelDestination.of(route)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppNavigationRailUiState(
                selected = TopLevelDestination.of(navigator.currentRoute.value)
            ),
        )

    fun onDestinationClick(destination: TopLevelDestination) {
        if (navigator.currentRoute.value != destination.route) {
            navigator.navigate {
                clear()
                add(destination.route)
            }
        }
    }
}
