package com.munzenberger.money.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val navigator: Navigator,
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: StateFlow<AppUiState> = repositoryController.moneyRepository
        .map { repository -> AppUiState(isRepositoryConnected = repository != null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppUiState(
                isRepositoryConnected = repositoryController.moneyRepository.value != null
            ),
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
}
