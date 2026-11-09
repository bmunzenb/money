package com.munzenberger.money.presentation.database

import com.munzenberger.money.core.demo.PopulateDemoDataResult
import com.munzenberger.money.core.demo.PopulateDemoDataUseCase
import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionResult
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
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

private val logger = Logger.getLogger(MoneyRepositoryController::class.java.name)

class MoneyRepositoryController(
    private val connectorFactory: MoneyRepositoryConnectorFactory,
    private val populateDemoData: PopulateDemoDataUseCase,
    private val context: CoroutineContext = Dispatchers.IO,
) {
    private val connectionStateFlow =
        MutableStateFlow<RepositoryConnectionState>(RepositoryConnectionState.Disconnected)
    val connectionState = connectionStateFlow.asStateFlow()

    // Lets one open or create run at a time, so overlapping calls can't each publish a repository and leak one.
    private val mutex = Mutex()

    suspend fun createDatabase(file: File): MoneyRepositoryConnectionResult = connect {
        try {
            Files.deleteIfExists(file.toPath())
        } catch (e: IOException) {
            return@connect MoneyRepositoryConnectionResult.Failure(e)
        }
        connectorFactory.create(file).connect()
    }

    suspend fun openDatabase(file: File): MoneyRepositoryConnectionResult = connect {
        connectorFactory.open(file).connect()
    }

    /**
     * Opens a new in-memory database populated with demo data, which is discarded when it's closed. The
     * repository is only published once its demo data is written; if that fails, it's closed and the result is
     * [MoneyRepositoryConnectionResult.Failure].
     */
    suspend fun openDemo(): MoneyRepositoryConnectionResult = connect {
        val result = connectorFactory.createInMemory().connect()
        if (result is MoneyRepositoryConnectionResult.Success) populateDemo(result) else result
    }

    // Closes the repository if its demo data isn't written, including when populating throws or is cancelled.
    private suspend fun populateDemo(
        connection: MoneyRepositoryConnectionResult.Success,
    ): MoneyRepositoryConnectionResult {
        var populated = false
        try {
            val result = populateDemoData(connection.moneyRepository)
            if (result is PopulateDemoDataResult.Failure) {
                return MoneyRepositoryConnectionResult.Failure(result.cause)
            }
            populated = true
            return connection
        } finally {
            if (!populated) connection.moneyRepository.close()
        }
    }

    /**
     * Closes the open repository and publishes [RepositoryConnectionState.Connecting], then publishes the repository
     * [open] returns if it's ready. If [open] fails, or throws, which is logged and returned as
     * [MoneyRepositoryConnectionResult.Failure], the controller is left disconnected.
     */
    private suspend fun connect(
        open: suspend () -> MoneyRepositoryConnectionResult,
    ): MoneyRepositoryConnectionResult = withContext(context) {
        mutex.withLock {
            closeRepository(next = RepositoryConnectionState.Connecting)
            var state: RepositoryConnectionState = RepositoryConnectionState.Disconnected
            try {
                open().also {
                    if (it is MoneyRepositoryConnectionResult.Success) {
                        state = RepositoryConnectionState.Connected(it.moneyRepository)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                logger.log(Level.WARNING, "Failed to connect to the money repository", e)
                MoneyRepositoryConnectionResult.Failure(e)
            } finally {
                connectionStateFlow.value = state
            }
        }
    }

    /**
     * Closes the open repository off the calling thread. It waits for an open or create in progress to finish, then
     * closes that repository, so a database that was opening when this was called doesn't stay open afterward.
     */
    suspend fun close() = withContext(context) {
        mutex.withLock { closeRepository() }
    }

    // Callers hold the mutex; it isn't reentrant, so connect can't call close. Publishes [next] before closing, so the
    // state never holds a closed repository, and logs a failed close rather than throwing, since the repository can't
    // be used either way.
    private fun closeRepository(next: RepositoryConnectionState = RepositoryConnectionState.Disconnected) {
        val repository = connectionStateFlow.value.repository
        connectionStateFlow.value = next
        try {
            repository?.close()
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.log(Level.WARNING, "Failed to close the money repository", e)
        }
    }
}

/**
 * The currently open [MoneyRepository], or `null` while none is open, including while one is connecting. Read it to
 * write through a use case; to read, use [flow] or [resultFlow], which follow the open repository.
 */
val MoneyRepositoryController.repository: MoneyRepository?
    get() = connectionState.value.repository

/**
 * Returns a flow of values selected from the currently open [MoneyRepository].
 *
 * The [selector] is applied to each repository as it's opened, and the flow switches to the new
 * repository's flow whenever the open repository changes. Nothing is emitted while no repository
 * is open.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> MoneyRepositoryController.flow(selector: (MoneyRepository) -> Flow<T>): Flow<T> =
    connectionState.flatMapLatest { state ->
        state.repository?.let(selector) ?: emptyFlow()
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
