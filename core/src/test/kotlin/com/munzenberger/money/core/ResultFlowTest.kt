package com.munzenberger.money.core

import app.cash.turbine.test
import com.munzenberger.money.data.api.EntityNotFoundException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResultFlowTest {

    @Test
    fun `asResult wraps each value in a success`() = runTest {
        flowOf(1, 2).asResult().test {
            assertEquals(Result.success(1), awaitItem())
            assertEquals(Result.success(2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `asResult emits failure when the flow throws`() = runTest {
        val error = IllegalStateException("query failed")

        flow<Int> { throw error }.asResult().test {
            assertEquals(Result.failure(error), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `requireFound passes non-null values through`() = runTest {
        flowOf(Result.success<Int?>(1)).requireFound { "missing" }.test {
            assertEquals(Result.success(1), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `requireFound replaces a null value with a not-found failure and keeps emitting`() = runTest {
        flowOf(Result.success<Int?>(null), Result.success(2)).requireFound { "missing" }.test {
            val error = awaitItem().exceptionOrNull()
            assertIs<EntityNotFoundException>(error)
            assertEquals("missing", error.message)
            assertEquals(Result.success(2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `requireFound passes failures through`() = runTest {
        val error = IllegalStateException("query failed")

        flowOf(Result.failure<Int?>(error)).requireFound { "missing" }.test {
            assertEquals(Result.failure(error), awaitItem())
            awaitComplete()
        }
    }
}
