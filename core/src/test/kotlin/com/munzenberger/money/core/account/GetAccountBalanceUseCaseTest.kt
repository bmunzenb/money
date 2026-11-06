package com.munzenberger.money.core.account

import app.cash.turbine.test
import com.munzenberger.money.data.api.EntityNotFoundException
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
import kotlin.test.assertIs

class GetAccountBalanceUseCaseTest {

    private val accountId = AccountId()

    private val balanceFlow = MutableStateFlow<Money?>(Money(100))

    private val repository = mockk<MoneyRepository> {
        every { accountBalanceById(accountId) } returns balanceFlow
    }

    private val getAccountBalance = GetAccountBalanceUseCase()

    @Test
    fun `emits the account's balance`() = runTest {
        getAccountBalance(repository, accountId).test {
            assertEquals(Result.success(Money(100)), awaitItem())
        }
    }

    @Test
    fun `emits the balance again when it changes`() = runTest {
        getAccountBalance(repository, accountId).test {
            awaitItem()

            balanceFlow.value = Money(150)

            assertEquals(Result.success(Money(150)), awaitItem())
        }
    }

    @Test
    fun `emits a not-found failure when there's no such account`() = runTest {
        balanceFlow.value = null

        getAccountBalance(repository, accountId).test {
            assertIs<EntityNotFoundException>(awaitItem().exceptionOrNull())
        }
    }

    @Test
    fun `keeps emitting after the account isn't found`() = runTest {
        getAccountBalance(repository, accountId).test {
            awaitItem()

            balanceFlow.value = null
            assertIs<EntityNotFoundException>(awaitItem().exceptionOrNull())

            // The flow is still collecting, so a later value still arrives.
            balanceFlow.value = Money(100)
            assertEquals(Result.success(Money(100)), awaitItem())
        }
    }

    @Test
    fun `emits failure when the balance flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.accountBalanceById(accountId) } returns flow { throw error }

        getAccountBalance(repository, accountId).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }
}
