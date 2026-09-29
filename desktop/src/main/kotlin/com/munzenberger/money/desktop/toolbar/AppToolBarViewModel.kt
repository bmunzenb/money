package com.munzenberger.money.desktop.toolbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.desktop.navigation.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppToolBarViewModel(
    private val navigator: Navigator
) : ViewModel() {

    val state: StateFlow<AppToolBarUiState> = navigator.canNavigateBack
        .map { isBackEnabled -> AppToolBarUiState(isBackEnabled = isBackEnabled) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppToolBarUiState(isBackEnabled = navigator.canNavigateBack.value),
        )

    fun onBackClick() {
        if (navigator.backStack.size > 1) {
            navigator.navigate { removeLast() }
        }
    }

    fun onAccountsClick() {
    }

    fun onCategoriesClick() {
    }

    fun onPayeesClick() {
    }
}
