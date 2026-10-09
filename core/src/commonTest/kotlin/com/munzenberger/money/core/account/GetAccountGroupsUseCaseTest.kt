package com.munzenberger.money.core.account

import app.cash.turbine.test
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.api.bank.BankId
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAccountGroupsUseCaseTest {

    private val assets = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets)
    private val liabilities = AccountClass(id = AccountClassId(2), value = AccountClassConstant.Liabilities)

    private val checking = AccountType(id = AccountTypeId(2), accountClass = assets, value = AccountTypeConstant.Checking)
    private val savings = AccountType(id = AccountTypeId(1), accountClass = assets, value = AccountTypeConstant.Savings)
    private val credit = AccountType(id = AccountTypeId(5), accountClass = liabilities, value = AccountTypeConstant.Credit)

    private val zephyr = Bank(name = "Zephyr Credit Union")
    private val acme = Bank(name = "acme Bank")

    private val visa = Account(name = "Visa", accountType = credit, bankId = acme.id)
    private val everyday = Account(name = "everyday", accountType = checking, bankId = zephyr.id)
    private val bills = Account(name = "Bills", accountType = checking)
    private val rainyDay = Account(name = "Rainy day", accountType = savings, bankId = acme.id)
    private val orphan = Account(name = "Orphan", accountType = savings, bankId = BankId())

    private val accounts = MutableStateFlow(listOf(visa, everyday, bills, rainyDay))
    private val banks = MutableStateFlow(listOf(zephyr, acme))

    private val repository = mockk<MoneyRepository> {
        every { accounts } returns this@GetAccountGroupsUseCaseTest.accounts
        every { banks } returns this@GetAccountGroupsUseCaseTest.banks
    }

    private val controller = controller(repository)

    private val getAccountGroups = GetAccountGroupsUseCase(controller)

    private fun controller(repository: MoneyRepository) = MoneyRepositoryController(
        connectorFactory = { mockk { coEvery { connect() } returns MoneyRepositoryConnectionStatus.Ready(repository) } },
        context = EmptyCoroutineContext,
    )

    private suspend fun MoneyRepositoryController.connect() {
        openDatabase(File("money-test.db"))
    }

    @Test
    fun testNoGroupingHasOneGroupWithAllAccountsSortedByName() = runTest {
        getAccountGroups(flowOf(AccountGrouping.None)).test {
            controller.connect()

            assertEquals(
                Result.success(listOf(AccountGroup.All(listOf(bills, everyday, rainyDay, visa)))),
                awaitItem(),
            )
        }
    }

    @Test
    fun testNoGroupingWithNoAccountsHasNoGroups() = runTest {
        accounts.value = emptyList()

        getAccountGroups(flowOf(AccountGrouping.None)).test {
            controller.connect()

            assertEquals(Result.success(emptyList()), awaitItem())
        }
    }

    @Test
    fun testGroupsByAccountTypeInConstantOrder() = runTest {
        getAccountGroups(flowOf(AccountGrouping.AccountType)).test {
            controller.connect()

            assertEquals(
                Result.success(
                    listOf(
                        AccountGroup.ByAccountType(savings, listOf(rainyDay)),
                        AccountGroup.ByAccountType(checking, listOf(bills, everyday)),
                        AccountGroup.ByAccountType(credit, listOf(visa)),
                    )
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun testGroupsByAccountClassInConstantOrder() = runTest {
        getAccountGroups(flowOf(AccountGrouping.AccountClass)).test {
            controller.connect()

            assertEquals(
                Result.success(
                    listOf(
                        AccountGroup.ByAccountClass(assets, listOf(bills, everyday, rainyDay)),
                        AccountGroup.ByAccountClass(liabilities, listOf(visa)),
                    )
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun testGroupsByBankSortedByNameWithNoBankLast() = runTest {
        accounts.value += orphan

        getAccountGroups(flowOf(AccountGrouping.Bank)).test {
            controller.connect()

            assertEquals(
                Result.success(
                    listOf(
                        AccountGroup.ByBank(acme, listOf(rainyDay, visa)),
                        AccountGroup.ByBank(zephyr, listOf(everyday)),
                        AccountGroup.ByBank(null, listOf(bills, orphan)),
                    )
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun testGroupingWithNoAccountsHasNoGroups() = runTest {
        accounts.value = emptyList()

        getAccountGroups(flowOf(AccountGrouping.Bank)).test {
            controller.connect()

            assertEquals(Result.success(emptyList()), awaitItem())
        }
    }

    @Test
    fun testEmitsNewGroupsWhenAccountsChange() = runTest {
        getAccountGroups(flowOf(AccountGrouping.AccountClass)).test {
            controller.connect()
            awaitItem()

            accounts.value = listOf(visa)

            assertEquals(Result.success(listOf(AccountGroup.ByAccountClass(liabilities, listOf(visa)))), awaitItem())
        }
    }

    @Test
    fun testRegroupsWithoutReadingAccountsAgainWhenGroupingChanges() = runTest {
        var accountReads = 0
        val counting = mockk<MoneyRepository> {
            every { accounts } returns this@GetAccountGroupsUseCaseTest.accounts.onStart { accountReads++ }
            every { banks } returns this@GetAccountGroupsUseCaseTest.banks
        }
        val controller = controller(counting)
        val grouping = MutableStateFlow(AccountGrouping.None)

        GetAccountGroupsUseCase(controller)(grouping).test {
            controller.connect()
            awaitItem()

            grouping.value = AccountGrouping.AccountClass

            assertEquals(
                Result.success(
                    listOf(
                        AccountGroup.ByAccountClass(assets, listOf(bills, everyday, rainyDay)),
                        AccountGroup.ByAccountClass(liabilities, listOf(visa)),
                    )
                ),
                awaitItem(),
            )
            assertEquals(1, accountReads)
        }
    }

    @Test
    fun testEmitsNewGroupsWhenBanksChange() = runTest {
        val renamed = acme.copy(name = "Zzz Bank")

        getAccountGroups(flowOf(AccountGrouping.Bank)).test {
            controller.connect()
            awaitItem()

            banks.value = listOf(zephyr, renamed)

            assertEquals(
                Result.success(
                    listOf(
                        AccountGroup.ByBank(zephyr, listOf(everyday)),
                        AccountGroup.ByBank(renamed, listOf(rainyDay, visa)),
                        AccountGroup.ByBank(null, listOf(bills)),
                    )
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun testEmitsFailureWhenAccountsFlowThrows() = runTest {
        val error = IllegalStateException("query failed")
        val failing = mockk<MoneyRepository> {
            every { accounts } returns flow { throw error }
            every { banks } returns flowOf(emptyList())
        }
        val controller = controller(failing)

        GetAccountGroupsUseCase(controller)(flowOf(AccountGrouping.Bank)).test {
            controller.connect()

            // Coroutines' stack trace recovery may copy the exception as it crosses combine.
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
        }
    }
}
