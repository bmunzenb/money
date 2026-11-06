package com.munzenberger.money.presentation.toolbar

/**
 * The state of the app's toolbar. [canAddTransaction] is false while the new transaction form is showing, so it
 * doesn't open another one on top of it.
 */
data class AppToolbarUiState(
    val canNavigateBack: Boolean = false,
    val canAddTransaction: Boolean = true,
)
