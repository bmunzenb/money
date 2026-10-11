package com.munzenberger.money.core.account

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.api.bank.BankId
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.runTest
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

    private val getAccountGroups = GetAccountGroupsUseCase()

    @Test
    fun testNoGroupingHasOneGroupWithAllAccountsSortedByName() = runTest {
        getAccountGroups(repository, flowOf(AccountGrouping.None)).test {
            assertEquals(
                Result.success(listOf(AccountGroup.All(listOf(bills, everyday, rainyDay, visa)))),
                awaitItem(),
            )
        }
    }

    @Test
    fun testNoGroupingWithNoAccountsHasNoGroups() = runTest {
        accounts.value = emptyList()

        getAccountGroups(repository, flowOf(AccountGrouping.None)).test {
            assertEquals(Result.success(emptyList()), awaitItem())
        }
    }

    @Test
    fun testGroupsByAccountTypeInConstantOrder() = runTest {
        getAccountGroups(repository, flowOf(AccountGrouping.AccountType)).test {
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
        getAccountGroups(repository, flowOf(AccountGrouping.AccountClass)).test {
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

        getAccountGroups(repository, flowOf(AccountGrouping.Bank)).test {
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

        getAccountGroups(repository, flowOf(AccountGrouping.Bank)).test {
            assertEquals(Result.success(emptyList()), awaitItem())
        }
    }

    @Test
    fun testEmitsNewGroupsWhenAccountsChange() = runTest {
        getAccountGroups(repository, flowOf(AccountGrouping.AccountClass)).test {
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
        val grouping = MutableStateFlow(AccountGrouping.None)

        getAccountGroups(counting, grouping).test {
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

        getAccountGroups(repository, flowOf(AccountGrouping.Bank)).test {
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

        getAccountGroups(failing, flowOf(AccountGrouping.Bank)).test {
            // Coroutines' stack trace recovery may copy the exception as it crosses combine.
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }
}
