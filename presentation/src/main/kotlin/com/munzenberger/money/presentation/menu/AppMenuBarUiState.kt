package com.munzenberger.money.presentation.menu

/**
 * The state of the app's menu bar. [canCloseRepository] is false while no repository is open, so there's nothing to
 * close.
 */
data class AppMenuBarUiState(
    val canCloseRepository: Boolean = false,
)
