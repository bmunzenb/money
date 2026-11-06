package com.munzenberger.money.core

import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.MoneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.logging.Level
import java.util.logging.Logger

private val logger = Logger.getLogger("com.munzenberger.money.core.MoneyRepositoryResultFlow")

/**
 * Returns the flow of values selected from this [MoneyRepository] by [selector], each wrapped in a
 * [Result].
 *
 * An exception thrown by the selected flow is logged and emitted as a [Result.failure], which ends
 * the flow.
 */
fun <T> MoneyRepository.resultFlow(selector: (MoneyRepository) -> Flow<T>): Flow<Result<T>> =
    selector(this)
        .map { value -> Result.success(value) }
        .catch { e ->
            logger.log(Level.WARNING, "Failed to read from the money repository", e)
            emit(Result.failure(e))
        }

/**
 * Returns this flow with each null value, such as an entity's after it's removed, replaced by a failure with an
 * [EntityNotFoundException] whose message is [message]'s. That isn't logged as a failed read, and it doesn't end the
 * flow.
 */
fun <T : Any> Flow<Result<T?>>.requireFound(message: () -> String): Flow<Result<T>> =
    map { result -> result.mapCatching { it ?: throw EntityNotFoundException(message()) } }
