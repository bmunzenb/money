package com.munzenberger.money.desktop.welcome

import org.jetbrains.compose.resources.StringResource

sealed interface WelcomeUiState {
    data object Idle : WelcomeUiState
    data object Loading : WelcomeUiState
    data class Error(val messageRes: StringResource) : WelcomeUiState
}
