package com.munzenberger.money.data.api

interface MoneyRepositoryConnector {
    suspend fun connect(): MoneyRepositoryConnectionResult
}

sealed interface MoneyRepositoryConnectionResult {
    data class Success(val moneyRepository: MoneyRepository) : MoneyRepositoryConnectionResult
    data class Failure(val cause: Throwable) : MoneyRepositoryConnectionResult
}
