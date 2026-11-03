package com.munzenberger.money.data.api

interface MoneyRepositoryConnector {
    suspend fun connect(): MoneyRepositoryConnectionStatus
}

sealed interface MoneyRepositoryConnectionStatus {
    data class Ready(val moneyRepository: MoneyRepository) : MoneyRepositoryConnectionStatus
    data class Failed(val error: Throwable) : MoneyRepositoryConnectionStatus
}
