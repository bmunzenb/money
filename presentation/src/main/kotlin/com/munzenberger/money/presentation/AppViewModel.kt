package com.munzenberger.money.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val navigator: Navigator,
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: StateFlow<AppUiState> =
        combine(repositoryController.moneyRepository, navigator.backStack, ::uiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = uiState(repositoryController.moneyRepository.value, navigator.backStack.value),
            )

    init {
        viewModelScope.launch {
            repositoryController.moneyRepository.collect { repository ->
                val route = if (repository != null) Route.AccountList else Route.Welcome
                navigator.navigate {
                    clear()
                    add(route)
                }
            }
        }
    }

    // Never pops the last route, so there's always a screen to show.
    fun navigateBack() {
        navigator.navigate { if (size > 1) removeAt(lastIndex) }
    }

    private fun uiState(repository: MoneyRepository?, backStack: List<Route>) = AppUiState(
        isRepositoryConnected = repository != null,
        canNavigateBack = backStack.size > 1,
    )
}
