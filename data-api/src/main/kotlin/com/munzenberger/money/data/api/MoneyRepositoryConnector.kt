package com.munzenberger.money.data.api

import java.io.File

interface MoneyRepositoryConnector {
    suspend fun create(): MoneyRepositoryConnectionStatus
    suspend fun connect(): MoneyRepositoryConnectionStatus
}

/** Creates [MoneyRepositoryConnector]s for a data implementation, which the app supplies. */
interface MoneyRepositoryConnectorFactory {
    /** Returns a connector to the database in [file]. */
    fun file(file: File): MoneyRepositoryConnector

    /** Returns a connector to a new, empty in-memory database, which is discarded when it's closed. */
    fun inMemory(): MoneyRepositoryConnector
}

sealed interface MoneyRepositoryConnectionStatus {
    data class Ready(val moneyRepository: MoneyRepository) : MoneyRepositoryConnectionStatus
    data object UnsupportedVersion : MoneyRepositoryConnectionStatus
    data class Failed(val error: Throwable) : MoneyRepositoryConnectionStatus

    interface RequiresMigration : MoneyRepositoryConnectionStatus {
        suspend fun migrate(): MoneyRepositoryConnectionStatus
    }
}
