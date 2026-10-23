package com.munzenberger.money.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() {

    private val stateFlow = MutableStateFlow<WelcomeUiState>(WelcomeUiState.Idle)
    val state: StateFlow<WelcomeUiState> = stateFlow.asStateFlow()

    fun createDatabase(file: File) {
        viewModelScope.launch {
            stateFlow.value = WelcomeUiState.Loading

            stateFlow.value = when (repositoryController.createDatabase(file)) {
                is MoneyRepositoryConnectionStatus.Ready -> WelcomeUiState.Loading
                is MoneyRepositoryConnectionStatus.Failed ->
                    WelcomeUiState.Error(WelcomeError.CreateDatabaseFailed)
                // Not yet supported; see MoneyRepositoryConnectionStatus.
                is MoneyRepositoryConnectionStatus.RequiresMigration,
                MoneyRepositoryConnectionStatus.UnsupportedVersion ->
                    error("Unhandled connection status")
            }
        }
    }

    fun openDatabase(file: File) {
        viewModelScope.launch {
            stateFlow.value = WelcomeUiState.Loading

            stateFlow.value = when (repositoryController.openDatabase(file)) {
                is MoneyRepositoryConnectionStatus.Ready -> WelcomeUiState.Loading
                is MoneyRepositoryConnectionStatus.Failed ->
                    WelcomeUiState.Error(WelcomeError.OpenDatabaseFailed)
                // Not yet supported; see MoneyRepositoryConnectionStatus.
                is MoneyRepositoryConnectionStatus.RequiresMigration,
                MoneyRepositoryConnectionStatus.UnsupportedVersion ->
                    error("Unhandled connection status")
            }
        }
    }
}
