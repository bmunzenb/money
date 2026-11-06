package com.munzenberger.money.core.register

import app.cash.turbine.test
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAccountRegisterUseCaseTest {

    private val accountId = AccountId()

    private val entry = registerEntry(amount = Money(-25), balance = Money(75))

    private val entriesFlow = MutableStateFlow<List<RegisterEntry>>(listOf(entry))

    private val repository = mockk<MoneyRepository> {
        every { registerEntriesForAccount(accountId) } returns entriesFlow
    }

    private val getAccountRegister = GetAccountRegisterUseCase()

    @Test
    fun `emits the account's register entries`() = runTest {
        getAccountRegister(repository, accountId).test {
            assertEquals(Result.success(listOf(entry)), awaitItem())
        }
    }

    @Test
    fun `emits the entries again when they change`() = runTest {
        getAccountRegister(repository, accountId).test {
            awaitItem()

            val added = registerEntry(amount = Money(50), balance = Money(125))
            entriesFlow.value = listOf(entry, added)

            assertEquals(Result.success(listOf(entry, added)), awaitItem())
        }
    }

    @Test
    fun `emits failure when the entries flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.registerEntriesForAccount(accountId) } returns flow { throw error }

        getAccountRegister(repository, accountId).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }

    private fun registerEntry(amount: Money, balance: Money) = RegisterEntry.Transaction(
        transactionId = TransactionId(),
        payeeId = null,
        payeeName = null,
        date = LocalDate(2026, 1, 15),
        amount = amount,
        balance = balance,
        number = null,
        memo = null,
        status = TransactionStatus(TransactionStatusId(1), TransactionStatusConstant.Unreconciled),
        entries = emptyList(),
    )
}
