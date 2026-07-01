package com.munzenberger.money.core

import com.munzenberger.money.data.api.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
