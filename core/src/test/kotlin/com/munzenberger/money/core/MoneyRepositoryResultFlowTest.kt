package com.munzenberger.money.core

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class MoneyRepositoryResultFlowTest {

    private val repository = mockk<MoneyRepository>()

    @Test
    fun `resultFlow applies selector to repository`() = runTest {
        repository.resultFlow { flowOf(it) }.test {
            assertSame(repository, awaitItem().getOrThrow())
            awaitComplete()
        }
    }

    @Test
    fun `resultFlow emits success from selected flow`() = runTest {
        repository.resultFlow { flowOf(1, 2) }.test {
            assertEquals(Result.success(1), awaitItem())
            assertEquals(Result.success(2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `resultFlow emits failure when selected flow throws`() = runTest {
        val error = IllegalStateException("query failed")

        repository.resultFlow<Int> { flow { throw error } }.test {
            assertEquals(Result.failure(error), awaitItem())
            awaitComplete()
        }
    }
}
