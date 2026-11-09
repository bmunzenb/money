package com.munzenberger.money.presentation.welcome

import com.munzenberger.money.presentation.database.DatabaseError

/**
 * [error] is shown over the screen until the user dismisses it with [WelcomeViewModel.dismissError],
 * or starts another operation.
 */
data class WelcomeUiState(
    val isLoading: Boolean = false,
    val error: DatabaseError? = null,
)
