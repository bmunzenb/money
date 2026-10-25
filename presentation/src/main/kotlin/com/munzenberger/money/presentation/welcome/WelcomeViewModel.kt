package com.munzenberger.money.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() {

    private val stateFlow = MutableStateFlow(WelcomeUiState())
    val state: StateFlow<WelcomeUiState> = stateFlow.asStateFlow()

    fun createDatabase(file: File) {
        connect(WelcomeError.CreateDatabaseFailed) { repositoryController.createDatabase(file) }
    }

    fun openDatabase(file: File) {
        connect(WelcomeError.OpenDatabaseFailed) { repositoryController.openDatabase(file) }
    }

    fun dismissError() {
        stateFlow.update { it.copy(error = null) }
    }

    private fun connect(
        failure: WelcomeError,
        connect: suspend () -> MoneyRepositoryConnectionStatus,
    ) {
        viewModelScope.launch {
            stateFlow.value = WelcomeUiState(isLoading = true)

            stateFlow.value = when (connect()) {
                // The screen is about to be replaced, so it keeps showing progress instead of its buttons.
                is MoneyRepositoryConnectionStatus.Ready -> WelcomeUiState(isLoading = true)
                is MoneyRepositoryConnectionStatus.Failed -> WelcomeUiState(error = failure)
                // Not yet supported; see MoneyRepositoryConnectionStatus.
                is MoneyRepositoryConnectionStatus.RequiresMigration,
                MoneyRepositoryConnectionStatus.UnsupportedVersion ->
                    error("Unhandled connection status")
            }
        }
    }
}
