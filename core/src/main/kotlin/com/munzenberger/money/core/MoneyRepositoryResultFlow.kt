package com.munzenberger.money.core

import com.munzenberger.money.data.api.MoneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.logging.Level

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
            this@resultFlow.logger.log(Level.WARNING, "Failed to read from the money repository", e)
            emit(Result.failure(e))
        }
