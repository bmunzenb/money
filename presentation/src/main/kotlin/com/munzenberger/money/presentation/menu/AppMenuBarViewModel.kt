package com.munzenberger.money.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The ViewModel for the app's menu bar. Which items each OS shows is up to the UI, following that platform's
 * conventions.
 */
class AppMenuBarViewModel(
    private val repositoryController: MoneyRepositoryController,
) : ViewModel() {

    val state: StateFlow<AppMenuBarUiState> = repositoryController.moneyRepository
        .map(::uiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = uiState(repositoryController.moneyRepository.value),
        )

    /** Closes the open repository, after which `AppViewModel` navigates to the welcome screen. */
    fun onCloseRepositoryClick() {
        repositoryController.close()
    }

    private fun uiState(repository: MoneyRepository?) = AppMenuBarUiState(
        canCloseRepository = repository != null,
    )
}
