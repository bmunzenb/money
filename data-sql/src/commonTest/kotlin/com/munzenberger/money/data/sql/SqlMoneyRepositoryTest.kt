package com.munzenberger.money.data.sql

import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.api.bank.BankId
import com.munzenberger.money.data.api.payee.Payee
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SqlMoneyRepositoryTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private fun createRepository(context: CoroutineDispatcher): SqlMoneyRepository {
        val driver = createTestJdbcDriver()
        MoneyDatabase.Schema.create(driver)
        return SqlMoneyRepository(name = "test", driver = driver, context = context)
    }

    @Test
    fun `transaction commits every write in the block`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "First Bank")
        val payee = Payee(name = "Grocer")
        repository.transaction {
            add(bank)
            add(payee)
        }
        assertEquals(listOf(bank), repository.banks.first())
        assertEquals(listOf(payee), repository.payees.first())
    }

    @Test
    fun `transaction returns the result of the block`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "First Bank")
        val result = repository.transaction {
            add(bank)
            bank.id
        }
        assertEquals(bank.id, result)
    }

    @Test
    fun `transaction rolls back every write when the block throws`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        assertFailsWith<IllegalStateException> {
            repository.transaction {
                add(Bank(name = "First Bank"))
                add(Payee(name = "Grocer"))
                error("boom")
            }
        }
        assertTrue(repository.banks.first().isEmpty())
        assertTrue(repository.payees.first().isEmpty())
    }

    @Test
    fun `transaction rolls back earlier writes when a later write fails`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        assertFailsWith<Exception> {
            repository.transaction {
                add(Bank(name = "First Bank"))
                // Violates the account's foreign key to an unknown bank.
                add(Account(name = "Checking", accountType = checking, bankId = BankId()))
            }
        }
        assertTrue(repository.banks.first().isEmpty())
        assertTrue(repository.accounts.first().isEmpty())
    }

    @Test
    fun `transaction rolls back earlier writes when an update finds no row`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        assertFailsWith<EntityNotFoundException> {
            repository.transaction {
                add(Bank(name = "First Bank"))
                update(Payee(name = "Never added"))
            }
        }
        assertTrue(repository.banks.first().isEmpty())
    }
}
