package com.munzenberger.money.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munzenberger.money.presentation.database.DatabaseRequest
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.RepositoryConnectionState
import com.munzenberger.money.presentation.database.execute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() {

    // This ViewModel's own operation, which stays loading until its result is known, so a failure goes straight
    // from progress to its error rather than briefly showing the buttons once the controller is disconnected. It's
    // only read and written on the main thread, by publish.
    private var operation = WelcomeUiState()

    private val stateFlow = MutableStateFlow(WelcomeUiState())
    val state: StateFlow<WelcomeUiState> = stateFlow.asStateFlow()

    init {
        publish()
        viewModelScope.launch {
            repositoryController.connectionState.collect { publish() }
        }
    }

    fun createDatabase(file: File) {
        execute(DatabaseRequest.Create(file))
    }

    fun openDatabase(file: File) {
        execute(DatabaseRequest.Open(file))
    }

    fun openDemo() {
        execute(DatabaseRequest.Demo)
    }

    fun dismissError() {
        publish(operation.copy(error = null))
    }

    private fun execute(request: DatabaseRequest) {
        viewModelScope.launch {
            publish(WelcomeUiState(isLoading = true))
            publish(WelcomeUiState(error = repositoryController.execute(request)))
        }
    }

    /**
     * Publishes [operation] with the controller's current connection: also loading while it connects or is
     * connected, such as for an open this ViewModel didn't start, or while the screen is about to be replaced after
     * a successful one. A connection replaces the last failure, so its error is cleared rather than shown over the
     * progress or brought back once that connection ends. Reading the connection here, rather than combining two
     * flows, keeps the error and the progress from being published out of step.
     */
    private fun publish(operation: WelcomeUiState = this.operation) {
        val isConnecting = repositoryController.connectionState.value != RepositoryConnectionState.Disconnected
        this.operation = if (isConnecting) operation.copy(error = null) else operation
        stateFlow.value = this.operation.copy(isLoading = this.operation.isLoading || isConnecting)
    }
}
