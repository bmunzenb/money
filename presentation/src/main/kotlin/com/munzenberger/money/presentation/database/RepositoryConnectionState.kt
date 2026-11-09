package com.munzenberger.money.presentation.database

import com.munzenberger.money.data.api.MoneyRepository

/** Whether [MoneyRepositoryController] has a repository open, or is opening one. */
sealed interface RepositoryConnectionState {
    /** The open repository, or `null` while none is open, including while one is connecting. */
    val repository: MoneyRepository?
        get() = (this as? Connected)?.repository

    data object Disconnected : RepositoryConnectionState

    /** An open or create is in progress; any repository that was open is already closed. */
    data object Connecting : RepositoryConnectionState

    data class Connected(override val repository: MoneyRepository) : RepositoryConnectionState
}
