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
    fun testResultFlowAppliesSelectorToRepository() = runTest {
        repository.resultFlow { flowOf(it) }.test {
            assertSame(repository, awaitItem().getOrThrow())
            awaitComplete()
        }
    }

    @Test
    fun testResultFlowEmitsSuccessFromSelectedFlow() = runTest {
        repository.resultFlow { flowOf(1, 2) }.test {
            assertEquals(Result.success(1), awaitItem())
            assertEquals(Result.success(2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testResultFlowEmitsFailureWhenSelectedFlowThrows() = runTest {
        val error = IllegalStateException("query failed")

        repository.resultFlow<Int> { flow { throw error } }.test {
            assertEquals(Result.failure(error), awaitItem())
            awaitComplete()
        }
    }
}
