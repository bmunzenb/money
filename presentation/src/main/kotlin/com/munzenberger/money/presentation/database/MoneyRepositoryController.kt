package com.munzenberger.money.presentation.database

import com.munzenberger.money.core.demo.PopulateDemoDataResult
import com.munzenberger.money.core.demo.PopulateDemoDataUseCase
import com.munzenberger.money.core.resultFlow
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
    private val demoConnectorFactory: () -> MoneyRepositoryConnector,
    private val populateDemoData: PopulateDemoDataUseCase,
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

    /**
     * Opens a new in-memory database populated with demo data, which is discarded when it's closed. The
     * repository is only published once its demo data is written; if that fails, it's closed and the result is
     * [MoneyRepositoryConnectionStatus.Failed].
     */
    suspend fun openDemo(): MoneyRepositoryConnectionStatus = withContext(context) {
        close()
        val status = demoConnectorFactory().create()
        if (status is MoneyRepositoryConnectionStatus.Ready) {
            val result = populateDemoData(status.moneyRepository)
            if (result is PopulateDemoDataResult.Failure) {
                status.moneyRepository.close()
                return@withContext MoneyRepositoryConnectionStatus.Failed(result.cause)
            }
        }
        status.also { applyConnectionStatus(it) }
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

/**
 * Returns a flow of values selected from the currently open [MoneyRepository].
 *
 * The [selector] is applied to each repository as it's opened, and the flow switches to the new
 * repository's flow whenever the open repository changes. Nothing is emitted while no repository
 * is open.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> MoneyRepositoryController.flow(selector: (MoneyRepository) -> Flow<T>): Flow<T> =
    moneyRepository.flatMapLatest { repository ->
        repository?.let(selector) ?: emptyFlow()
    }

/**
 * Returns a flow of values selected from the currently open [MoneyRepository], each wrapped in a
 * [Result] (see [flow] and [MoneyRepository.resultFlow]).
 *
 * A failure ends only that repository's flow, so opening a different repository resumes emitting
 * values.
 */
fun <T> MoneyRepositoryController.resultFlow(selector: (MoneyRepository) -> Flow<T>): Flow<Result<T>> =
    flow { it.resultFlow(selector) }
