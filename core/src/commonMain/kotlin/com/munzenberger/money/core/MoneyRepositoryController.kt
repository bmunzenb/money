package com.munzenberger.money.core

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

class MoneyRepositoryController(
    private val connectorFactory: (File) -> MoneyRepositoryConnector,
    private val context: CoroutineContext = Dispatchers.IO,
) {
    private val moneyRepositoryFlow = MutableStateFlow<MoneyRepository?>(null)
    val moneyRepository = moneyRepositoryFlow.asStateFlow()

    suspend fun createDatabase(file: File): MoneyRepositoryConnectionStatus = withContext(context) {
        close()
        if (file.exists()) {
            file.delete()
        }
        connectorFactory(file).create().also { applyConnectionStatus(it) }
    }

    suspend fun openDatabase(file: File): MoneyRepositoryConnectionStatus = withContext(context) {
        close()
        connectorFactory(file).connect().also { applyConnectionStatus(it) }
    }

    fun close() {
        moneyRepositoryFlow.value?.close()
        moneyRepositoryFlow.value = null
    }

    private fun applyConnectionStatus(status: MoneyRepositoryConnectionStatus) {
        if (status is MoneyRepositoryConnectionStatus.Ready) {
            moneyRepositoryFlow.value = status.moneyRepository
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> MoneyRepositoryController.flow(selector: (MoneyRepository) -> Flow<T>): Flow<T> =
    moneyRepository.flatMapLatest { repository -> repository?.let(selector) ?: emptyFlow() }
