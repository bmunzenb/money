package com.munzenberger.money.presentation.menu

import com.munzenberger.money.presentation.database.DatabaseError

/**
 * The state of the app's menu bar. [canCloseRepository] is false while no repository is open, so there's nothing to
 * close, and [canOpenDatabase] is false while one is opening, so the menu can't start another. [error] is why the
 * last database the menu asked for didn't open, shown until the user dismisses it with
 * [AppMenuBarViewModel.dismissError] or asks for another.
 */
data class AppMenuBarUiState(
    val canCloseRepository: Boolean = false,
    val canOpenDatabase: Boolean = true,
    val error: DatabaseError? = null,
)
