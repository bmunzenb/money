package com.munzenberger.money.presentation.welcome

sealed interface WelcomeUiState {
    data object Idle : WelcomeUiState
    data object Loading : WelcomeUiState
    data class Error(val error: WelcomeError) : WelcomeUiState
}

enum class WelcomeError {
    CreateDatabaseFailed,
    OpenDatabaseFailed,
}
