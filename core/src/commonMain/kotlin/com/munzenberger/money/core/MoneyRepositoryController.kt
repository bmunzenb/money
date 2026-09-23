package com.munzenberger.money.core

import com.munzenberger.money.data.api.MoneyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

class MoneyRepositoryController {
    private val moneyRepositoryFlow = MutableStateFlow<MoneyRepository?>(null)
    val moneyRepository = moneyRepositoryFlow.asStateFlow()

    fun update(moneyRepository: MoneyRepository) {
        moneyRepositoryFlow.update { moneyRepository }
    }

    fun clear() {
        moneyRepositoryFlow.update { null }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> MoneyRepositoryController.flow(selector: (MoneyRepository) -> Flow<T>): Flow<T> =
    moneyRepository.flatMapLatest { repository -> repository?.let(selector) ?: emptyFlow() }

fun MoneyRepositoryController.close() {
    moneyRepository.value?.close()
    clear()
}
