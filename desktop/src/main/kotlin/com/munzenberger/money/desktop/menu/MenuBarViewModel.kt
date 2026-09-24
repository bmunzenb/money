package com.munzenberger.money.desktop.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.close
import com.munzenberger.money.desktop.database.createDatabase
import com.munzenberger.money.desktop.database.openDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

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

    fun onNewDatabaseSelected(file: File) {
        viewModelScope.launch {
            repositoryController.createDatabase(file)
        }
    }

    fun onOpenDatabaseSelected(file: File) {
        viewModelScope.launch {
            repositoryController.openDatabase(file)
        }
    }

    fun onCloseDatabaseSelected() {
        repositoryController.close()
    }

    fun onExitSelected() {
        repositoryController.close()
    }
}
