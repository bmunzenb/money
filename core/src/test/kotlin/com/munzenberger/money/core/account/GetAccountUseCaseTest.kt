package com.munzenberger.money.core.account

import app.cash.turbine.test
import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetAccountUseCaseTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Asset),
        value = AccountTypeConstant.Checking,
    )

    private val account = Account(name = "Checking", accountType = checking)

    private val accountFlow = MutableStateFlow<Account?>(account)

    private val repository = mockk<MoneyRepository> {
        every { accountById(account.id) } returns accountFlow
    }

    private val getAccount = GetAccountUseCase()

    @Test
    fun `emits the account`() = runTest {
        getAccount(repository, account.id).test {
            assertEquals(Result.success(account), awaitItem())
        }
    }

    @Test
    fun `emits the account again when it changes`() = runTest {
        getAccount(repository, account.id).test {
            awaitItem()

            val renamed = account.copy(name = "Everyday")
            accountFlow.value = renamed

            assertEquals(Result.success(renamed), awaitItem())
        }
    }

    @Test
    fun `emits a not-found failure when there's no such account`() = runTest {
        accountFlow.value = null

        getAccount(repository, account.id).test {
            assertIs<EntityNotFoundException>(awaitItem().exceptionOrNull())
        }
    }

    @Test
    fun `keeps emitting after the account isn't found`() = runTest {
        getAccount(repository, account.id).test {
            awaitItem()

            accountFlow.value = null
            assertIs<EntityNotFoundException>(awaitItem().exceptionOrNull())

            // The flow is still collecting, so a later value still arrives.
            accountFlow.value = account
            assertEquals(Result.success(account), awaitItem())
        }
    }

    @Test
    fun `emits failure when the account flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.accountById(account.id) } returns flow { throw error }

        getAccount(repository, account.id).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }
}
