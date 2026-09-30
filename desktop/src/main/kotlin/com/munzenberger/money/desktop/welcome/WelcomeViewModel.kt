package com.munzenberger.money.desktop.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import money.shared.generated.resources.Res
import money.shared.generated.resources.create_database_error_message
import money.shared.generated.resources.open_database_error_message
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
                is MoneyRepositoryConnectionStatus.Ready -> WelcomeUiState.Idle
                is MoneyRepositoryConnectionStatus.Failed ->
                    WelcomeUiState.Error(Res.string.create_database_error_message)
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
                is MoneyRepositoryConnectionStatus.Ready -> WelcomeUiState.Idle
                is MoneyRepositoryConnectionStatus.Failed ->
                    WelcomeUiState.Error(Res.string.open_database_error_message)
                // Not yet supported; see MoneyRepositoryConnectionStatus.
                is MoneyRepositoryConnectionStatus.RequiresMigration,
                MoneyRepositoryConnectionStatus.UnsupportedVersion ->
                    error("Unhandled connection status")
            }
        }
    }
}
