package com.munzenberger.money.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.RepositoryConnectionState
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val navigator: Navigator,
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: StateFlow<AppUiState> = repositoryController.connectionState
        .map(::uiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = uiState(repositoryController.connectionState.value),
        )

    init {
        viewModelScope.launch {
            // Navigates on every change, so a new database always starts at AccountList, even if the collector
            // misses the Connecting in between. Connecting shows Welcome too, with its progress.
            repositoryController.connectionState.collect { connection ->
                val route = if (connection is RepositoryConnectionState.Connected) Route.AccountList else Route.Welcome
                navigator.navigate {
                    clear()
                    add(route)
                }
            }
        }
    }

    private fun uiState(connection: RepositoryConnectionState) = AppUiState(
        isRepositoryConnected = connection is RepositoryConnectionState.Connected,
    )
}
