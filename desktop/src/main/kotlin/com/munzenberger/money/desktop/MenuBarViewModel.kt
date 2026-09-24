package com.munzenberger.money.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.close
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MenuBarViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: StateFlow<MenuBarUiState> = repositoryController.moneyRepository
        .map { repository -> MenuBarUiState(closeRepositoryEnabled = repository != null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = MenuBarUiState(
                closeRepositoryEnabled = repositoryController.moneyRepository.value != null
            ),
        )

    fun onCloseDatabaseSelected() {
        repositoryController.close()
    }

    fun onExitSelected() {
        repositoryController.close()
    }
}
