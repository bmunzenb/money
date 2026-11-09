package com.munzenberger.money.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.presentation.database.DatabaseError
import com.munzenberger.money.presentation.database.DatabaseRequest
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.RepositoryConnectionState
import com.munzenberger.money.presentation.database.execute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 * The ViewModel for the app's menu bar. Which items each OS shows is up to the UI, following that platform's
 * conventions.
 */
class AppMenuBarViewModel(
    private val repositoryController: MoneyRepositoryController,
) : ViewModel() {

    private val errorFlow = MutableStateFlow<DatabaseError?>(null)

    val state: StateFlow<AppMenuBarUiState> = combine(repositoryController.connectionState, errorFlow, ::uiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = uiState(repositoryController.connectionState.value, errorFlow.value),
        )

    /** Creates a new database at [file], as the welcome screen does, closing the open repository first. */
    fun createDatabase(file: File) {
        execute(DatabaseRequest.Create(file))
    }

    /** Opens the database at [file], as the welcome screen does, closing the open repository first. */
    fun openDatabase(file: File) {
        execute(DatabaseRequest.Open(file))
    }

    /** Closes the open repository, after which `AppViewModel` navigates to the welcome screen. */
    fun onCloseRepositoryClick() {
        viewModelScope.launch {
            repositoryController.close()
        }
    }

    fun dismissError() {
        errorFlow.value = null
    }

    private fun execute(request: DatabaseRequest) {
        viewModelScope.launch {
            errorFlow.value = null
            errorFlow.value = repositoryController.execute(request)
        }
    }

    private fun uiState(connection: RepositoryConnectionState, error: DatabaseError?) = AppMenuBarUiState(
        canCloseRepository = connection is RepositoryConnectionState.Connected,
        canOpenDatabase = connection !is RepositoryConnectionState.Connecting,
        error = error,
    )
}
