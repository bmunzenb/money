package com.munzenberger.money.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.RepositoryConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() {

    // This ViewModel's own operation, which stays loading until its result is known, so a failure goes straight
    // from progress to its error rather than briefly showing the buttons once the controller is disconnected.
    private val operationState = MutableStateFlow(WelcomeUiState())

    // Also loading while the controller connects or is connected, such as for an open this ViewModel didn't start,
    // or while the screen is about to be replaced after a successful one.
    val state: StateFlow<WelcomeUiState> = combine(
        operationState,
        repositoryController.connectionState,
        ::uiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = uiState(operationState.value, repositoryController.connectionState.value),
    )

    fun createDatabase(file: File) {
        connect(WelcomeError.CreateDatabaseFailed) { repositoryController.createDatabase(file) }
    }

    fun openDatabase(file: File) {
        connect(WelcomeError.OpenDatabaseFailed) { repositoryController.openDatabase(file) }
    }

    fun openDemo() {
        connect(WelcomeError.OpenDemoFailed) { repositoryController.openDemo() }
    }

    fun dismissError() {
        operationState.update { it.copy(error = null) }
    }

    private fun connect(
        failure: WelcomeError,
        connect: suspend () -> MoneyRepositoryConnectionStatus,
    ) {
        viewModelScope.launch {
            operationState.value = WelcomeUiState(isLoading = true)

            operationState.value = when (connect()) {
                is MoneyRepositoryConnectionStatus.Ready -> WelcomeUiState()
                is MoneyRepositoryConnectionStatus.Failed -> WelcomeUiState(error = failure)
            }
        }
    }

    private fun uiState(operation: WelcomeUiState, connection: RepositoryConnectionState) = operation.copy(
        isLoading = operation.isLoading || connection != RepositoryConnectionState.Disconnected,
    )
}
