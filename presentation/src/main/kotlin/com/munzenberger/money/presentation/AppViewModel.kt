package com.munzenberger.money.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.RepositoryConnectionState
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
            // Connecting shows Welcome too, with its progress. Distinct, so a failed open (Connecting, then
            // Disconnected) doesn't navigate to Welcome again.
            repositoryController.connectionState
                .map { if (it is RepositoryConnectionState.Connected) Route.AccountList else Route.Welcome }
                .distinctUntilChanged()
                .collect { route ->
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
