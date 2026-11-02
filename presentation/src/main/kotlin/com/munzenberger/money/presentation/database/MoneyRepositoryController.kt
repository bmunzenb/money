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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.coroutines.CoroutineContext

class MoneyRepositoryController(
    private val fileConnectorFactory: (File) -> MoneyRepositoryConnector,
    private val demoConnectorFactory: () -> MoneyRepositoryConnector,
    private val populateDemoData: PopulateDemoDataUseCase,
    private val context: CoroutineContext = Dispatchers.IO,
) {
    private val moneyRepositoryFlow = MutableStateFlow<MoneyRepository?>(null)
    val moneyRepository = moneyRepositoryFlow.asStateFlow()

    // Lets one open or create run at a time, so overlapping calls can't each publish a repository and leak one.
    private val mutex = Mutex()

    suspend fun createDatabase(file: File): MoneyRepositoryConnectionStatus = connect {
        try {
            Files.deleteIfExists(file.toPath())
        } catch (e: IOException) {
            return@connect MoneyRepositoryConnectionStatus.Failed(e)
        }
        fileConnectorFactory(file).create()
    }

    suspend fun openDatabase(file: File): MoneyRepositoryConnectionStatus = connect {
        fileConnectorFactory(file).connect()
    }

    /**
     * Opens a new in-memory database populated with demo data, which is discarded when it's closed. The
     * repository is only published once its demo data is written; if that fails, it's closed and the result is
     * [MoneyRepositoryConnectionStatus.Failed].
     */
    suspend fun openDemo(): MoneyRepositoryConnectionStatus = connect {
        val status = demoConnectorFactory().create()
        if (status is MoneyRepositoryConnectionStatus.Ready) populateDemo(status) else status
    }

    // Closes the repository if its demo data isn't written, including when populating throws or is cancelled.
    private suspend fun populateDemo(
        status: MoneyRepositoryConnectionStatus.Ready,
    ): MoneyRepositoryConnectionStatus {
        var populated = false
        try {
            val result = populateDemoData(status.moneyRepository)
            if (result is PopulateDemoDataResult.Failure) {
                return MoneyRepositoryConnectionStatus.Failed(result.cause)
            }
            populated = true
            return status
        } finally {
            if (!populated) status.moneyRepository.close()
        }
    }

    // Closes the open repository, then publishes the one [open] returns if it's ready.
    private suspend fun connect(
        open: suspend () -> MoneyRepositoryConnectionStatus,
    ): MoneyRepositoryConnectionStatus = withContext(context) {
        mutex.withLock {
            close()
            open().also { applyConnectionStatus(it) }
        }
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
