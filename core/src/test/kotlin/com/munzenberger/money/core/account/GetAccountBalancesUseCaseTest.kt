package com.munzenberger.money.core.account

import app.cash.turbine.test
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAccountBalancesUseCaseTest {

    private val everyday = AccountId()
    private val bills = AccountId()

    private val balances = MutableStateFlow(mapOf(everyday to Money(100), bills to Money(200)))

    private val repository = mockk<MoneyRepository> {
        every { accountBalances } returns this@GetAccountBalancesUseCaseTest.balances
    }

    private val getAccountBalances = GetAccountBalancesUseCase()

    @Test
    fun `emits the balances`() = runTest {
        getAccountBalances(repository).test {
            assertEquals(Result.success(mapOf(everyday to Money(100), bills to Money(200))), awaitItem())
        }
    }

    @Test
    fun `emits new balances when they change`() = runTest {
        getAccountBalances(repository).test {
            awaitItem()

            balances.value = mapOf(everyday to Money(150), bills to Money(200))

            assertEquals(Result.success(mapOf(everyday to Money(150), bills to Money(200))), awaitItem())
        }
    }

    @Test
    fun `emits failure when balances flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.accountBalances } returns flow { throw error }

        getAccountBalances(repository).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }
}
