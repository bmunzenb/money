package com.munzenberger.money.core.demo

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PopulateDemoDataUseCaseTest {

    private val accountTypes = AccountTypeConstant.entries.mapIndexed { index, value ->
        val accountClass = when (value) {
            AccountTypeConstant.Credit, AccountTypeConstant.Loan ->
                AccountClass(id = AccountClassId(2), value = AccountClassConstant.Liabilities)
            else -> AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets)
        }
        AccountType(id = AccountTypeId(index + 1L), accountClass = accountClass, value = value)
    }

    private val banks = mutableListOf<Bank>()
    private val accounts = mutableListOf<Account>()
    private val writer = mockk<MoneyWriter> {
        every { add(capture(banks)) } returns Unit
        every { add(capture(accounts)) } returns Unit
    }
    private val repository = mockk<MoneyRepository> {
        every { this@mockk.accountTypes } returns flowOf(this@PopulateDemoDataUseCaseTest.accountTypes)
        coEvery { transaction<Unit>(any()) } answers { firstArg<MoneyWriter.() -> Unit>().invoke(writer) }
    }

    private val populateDemoData = PopulateDemoDataUseCase()

    @Test
    fun testAddsSeveralBanksEachWithAFewAccounts() = runTest {
        val result = populateDemoData(repository)

        assertEquals(PopulateDemoDataResult.Success, result)
        assertTrue(banks.size > 1)
        banks.forEach { bank ->
            assertTrue(accounts.count { it.bankId == bank.id } > 1, "${bank.name} has too few accounts")
        }
    }

    @Test
    fun testEveryAccountBelongsToAnAddedBank() = runTest {
        populateDemoData(repository)

        val bankIds = banks.map { it.id }.toSet()
        assertTrue(accounts.all { it.bankId in bankIds })
    }

    @Test
    fun testAccountsUseVaryingTypesFromTheRepository() = runTest {
        populateDemoData(repository)

        assertTrue(accounts.all { it.accountType in accountTypes })
        assertTrue(accounts.map { it.accountType }.distinct().size > 1)
    }

    @Test
    fun testAddsEachBankBeforeItsAccounts() = runTest {
        populateDemoData(repository)

        verifyOrder {
            banks.forEach { bank ->
                writer.add(bank)
                accounts.filter { it.bankId == bank.id }.forEach { writer.add(it) }
            }
        }
    }

    @Test
    fun testReturnsFailureWhenTheWriteFails() = runTest {
        val error = IllegalStateException("write failed")
        coEvery { repository.transaction<Unit>(any()) } throws error

        val result = populateDemoData(repository)

        assertEquals(PopulateDemoDataResult.Failure(error), result)
    }

    @Test
    fun testReturnsFailureWhenAnAccountTypeIsMissing() = runTest {
        every { repository.accountTypes } returns flowOf(emptyList())

        val result = populateDemoData(repository)

        assertIs<PopulateDemoDataResult.Failure>(result)
    }
}
