package com.munzenberger.money.core.register

import app.cash.turbine.test
import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetAccountRegisterUseCaseTest {

    private val account = Account(
        name = "Checking",
        accountType = AccountType(
            id = AccountTypeId(2),
            accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Asset),
            value = AccountTypeConstant.Checking,
        ),
    )

    private val accountId = account.id

    private val accountFlow = MutableStateFlow<Account?>(account)

    private val today = LocalDate(2026, 5, 15)

    private val dayBefore12Months = registerEntry(LocalDate(2025, 5, 14))
    private val first12Months = registerEntry(LocalDate(2025, 5, 15))
    private val dayBefore3Months = registerEntry(LocalDate(2026, 2, 14))
    private val first3Months = registerEntry(LocalDate(2026, 2, 15))
    private val earlierThisMonth = registerEntry(LocalDate(2026, 5, 1))
    private val laterThisMonth = registerEntry(LocalDate(2026, 5, 28))
    private val nextMonth = registerEntry(LocalDate(2026, 6, 10))
    private val nextYear = registerEntry(LocalDate(2027, 1, 5), balance = Money(150))

    private val allEntries = listOf(
        dayBefore12Months,
        first12Months,
        dayBefore3Months,
        first3Months,
        earlierThisMonth,
        laterThisMonth,
        nextMonth,
        nextYear,
    )

    private val entriesFlow = MutableStateFlow<List<RegisterEntry>>(allEntries)

    private val repository = mockk<MoneyRepository> {
        every { accountById(accountId) } returns accountFlow
        every { registerEntriesForAccount(accountId) } returns entriesFlow
    }

    private val getAccountRegister = GetAccountRegisterUseCase(today = { today })

    @Test
    fun `all dates and all transactions emits all of the account's register entries`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.AllDates),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertEquals(filtered(allEntries), awaitItem())
        }
    }

    @Test
    fun `current month emits the entries in today's month, including later dates`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.CurrentMonth),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertEquals(filtered(listOf(earlierThisMonth, laterThisMonth)), awaitItem())
        }
    }

    @Test
    fun `current year emits the entries in today's year, including later dates`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.CurrentYear),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertEquals(
                filtered(listOf(dayBefore3Months, first3Months, earlierThisMonth, laterThisMonth, nextMonth)),
                awaitItem(),
            )
        }
    }

    @Test
    fun `last 3 months emits the entries from 3 months ago on, including future dates`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.Last3Months),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertEquals(
                filtered(listOf(first3Months, earlierThisMonth, laterThisMonth, nextMonth, nextYear)),
                awaitItem(),
            )
        }
    }

    @Test
    fun `last 12 months emits the entries from 12 months ago on, including future dates`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.Last12Months),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertEquals(filtered(allEntries - dayBefore12Months), awaitItem())
        }
    }

    @Test
    fun `unreconciled transactions emits the entries that aren't reconciled`() = runTest {
        val unreconciled = registerEntry(today, TransactionStatusConstant.Unreconciled)
        val cleared = registerEntry(today, TransactionStatusConstant.Cleared)
        val reconciled = registerEntry(today, TransactionStatusConstant.Reconciled)
        entriesFlow.value = listOf(unreconciled, cleared, reconciled)

        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.AllDates),
            flowOf(RegisterStatusFilter.UnreconciledTransactions),
        ).test {
            assertEquals(filtered(listOf(unreconciled, cleared)), awaitItem())
        }
    }

    @Test
    fun `emits the entries that match both filters`() = runTest {
        val reconciledThisMonth = registerEntry(today, TransactionStatusConstant.Reconciled)
        val reconciledLastYear = registerEntry(LocalDate(2025, 12, 1), TransactionStatusConstant.Reconciled)
        val unreconciledLastYear = registerEntry(LocalDate(2025, 12, 1))
        entriesFlow.value = listOf(earlierThisMonth, reconciledThisMonth, reconciledLastYear, unreconciledLastYear)

        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.CurrentMonth),
            flowOf(RegisterStatusFilter.UnreconciledTransactions),
        ).test {
            assertEquals(filtered(listOf(earlierThisMonth)), awaitItem())
        }
    }

    @Test
    fun `emits the filtered entries again when they change`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.CurrentMonth),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            awaitItem()

            val added = registerEntry(LocalDate(2026, 5, 20))
            entriesFlow.value = allEntries + added

            assertEquals(filtered(listOf(earlierThisMonth, laterThisMonth, added)), awaitItem())
        }
    }

    @Test
    fun `refilters without reading entries again when the date filter changes`() = runTest {
        var entryReads = 0
        every { repository.registerEntriesForAccount(accountId) } returns entriesFlow.onStart { entryReads++ }
        val dateFilter = MutableStateFlow(RegisterDateFilter.AllDates)

        getAccountRegister(repository, accountId, dateFilter, flowOf(RegisterStatusFilter.AllTransactions)).test {
            awaitItem()

            dateFilter.value = RegisterDateFilter.CurrentMonth

            assertEquals(filtered(listOf(earlierThisMonth, laterThisMonth)), awaitItem())
            assertEquals(1, entryReads)
        }
    }

    @Test
    fun `refilters without reading entries again when the status filter changes`() = runTest {
        val unreconciled = registerEntry(today, TransactionStatusConstant.Unreconciled)
        val reconciled = registerEntry(today, TransactionStatusConstant.Reconciled)
        entriesFlow.value = listOf(unreconciled, reconciled)
        var entryReads = 0
        every { repository.registerEntriesForAccount(accountId) } returns entriesFlow.onStart { entryReads++ }
        val statusFilter = MutableStateFlow(RegisterStatusFilter.AllTransactions)

        getAccountRegister(repository, accountId, flowOf(RegisterDateFilter.AllDates), statusFilter).test {
            awaitItem()

            statusFilter.value = RegisterStatusFilter.UnreconciledTransactions

            assertEquals(filtered(listOf(unreconciled)), awaitItem())
            assertEquals(1, entryReads)
        }
    }

    @Test
    fun `filters by today's date when the entries are filtered`() = runTest {
        var currentDay = today
        val getAccountRegister = GetAccountRegisterUseCase(today = { currentDay })
        val dateFilter = MutableStateFlow(RegisterDateFilter.AllDates)

        getAccountRegister(repository, accountId, dateFilter, flowOf(RegisterStatusFilter.AllTransactions)).test {
            awaitItem()

            currentDay = LocalDate(2026, 6, 1)
            dateFilter.value = RegisterDateFilter.CurrentMonth

            assertEquals(filtered(listOf(nextMonth)), awaitItem())
        }
    }

    @Test
    fun `ending balance is the last entry's balance when the filters leave it out`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.CurrentMonth),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertEquals(Money(150), awaitItem().getOrThrow().endingBalance)
        }
    }

    @Test
    fun `ending balance is the last entry's balance when no entries match the filters`() = runTest {
        entriesFlow.value = listOf(registerEntry(today, TransactionStatusConstant.Reconciled, Money(90)))

        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.AllDates),
            flowOf(RegisterStatusFilter.UnreconciledTransactions),
        ).test {
            assertEquals(Result.success(AccountRegister(account, emptyList(), Money(90))), awaitItem())
        }
    }

    @Test
    fun `ending balance is the account's initial balance when it has no entries`() = runTest {
        accountFlow.value = account.copy(initialBalance = Money(1000))
        entriesFlow.value = emptyList()

        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.AllDates),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertEquals(Money(1000), awaitItem().getOrThrow().endingBalance)
        }
    }

    @Test
    fun `emits failure when the entries flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.registerEntriesForAccount(accountId) } returns flow { throw error }

        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.AllDates),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }

    @Test
    fun `emits the account again when it changes`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.AllDates),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            awaitItem()

            val renamed = account.copy(name = "Everyday")
            accountFlow.value = renamed

            assertEquals(renamed, awaitItem().getOrThrow().account)
        }
    }

    @Test
    fun `emits a not-found failure when there's no such account`() = runTest {
        accountFlow.value = null

        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.AllDates),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            assertIs<EntityNotFoundException>(awaitItem().exceptionOrNull())
        }
    }

    @Test
    fun `keeps emitting after the account isn't found`() = runTest {
        getAccountRegister(
            repository,
            accountId,
            flowOf(RegisterDateFilter.AllDates),
            flowOf(RegisterStatusFilter.AllTransactions),
        ).test {
            awaitItem()

            accountFlow.value = null
            assertIs<EntityNotFoundException>(awaitItem().exceptionOrNull())

            // The flow is still collecting, so a later value still arrives.
            accountFlow.value = account
            assertEquals(filtered(allEntries), awaitItem())
        }
    }

    // The entries that match the filters, with the ending balance of all the account's entries.
    private fun filtered(entries: List<RegisterEntry>) =
        Result.success(
            AccountRegister(account, entries, entriesFlow.value.lastOrNull()?.balance ?: account.initialBalance),
        )

    private fun registerEntry(
        date: LocalDate,
        status: TransactionStatusConstant = TransactionStatusConstant.Unreconciled,
        balance: Money = Money(75),
    ) = RegisterEntry.Transaction(
        transactionId = TransactionId(),
        payeeId = null,
        payeeName = null,
        date = date,
        amount = Money(-25),
        balance = balance,
        number = null,
        memo = null,
        status = TransactionStatus(TransactionStatusId(status.ordinal.toLong() + 1), status),
        entries = emptyList(),
    )
}
