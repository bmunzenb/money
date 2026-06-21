package com.munzenberger.money.data.sql.account

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.*
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.sql.bank.SqlBankRepository
import com.munzenberger.money.data.sql.createTestDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SqlAccountRepositoryTest {

    private val assets = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets)
    private val liabilities = AccountTypeGroup(id = AccountTypeGroupId(2), value = AccountTypeGroupConstant.Liabilities)

    private val checking = AccountType(id = AccountTypeId(2), group = assets, value = AccountTypeConstant.Checking)
    private val credit = AccountType(id = AccountTypeId(5), group = liabilities, value = AccountTypeConstant.Credit)

    private fun createRepository(context: CoroutineDispatcher): SqlAccountRepository =
        SqlAccountRepository(createTestDatabase(), context)

    @Test
    fun `accounts emits empty list when no accounts exist`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        assertTrue(repository.accounts.first().isEmpty())
    }

    @Test
    fun `add inserts an account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "Checking", accountType = checking)
        repository.add(account)
        assertEquals(listOf(account), repository.accounts.first())
    }

    @Test
    fun `add inserts an account with a number`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "Checking", number = "1234", accountType = checking)
        repository.add(account)
        assertEquals(listOf(account), repository.accounts.first())
    }

    @Test
    fun `add inserts an account with a bank`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val bank = Bank(name = "First Bank")
        SqlBankRepository(database, dispatcher).add(bank)
        val repository = SqlAccountRepository(database, dispatcher)
        val account = Account(name = "Checking", accountType = checking, bankId = bank.id)
        repository.add(account)
        assertEquals(listOf(account), repository.accounts.first())
    }

    @Test
    fun `add inserts an account with an initial balance`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "Checking", accountType = checking, initialBalance = Money(1000))
        repository.add(account)
        assertEquals(listOf(account), repository.accounts.first())
    }

    @Test
    fun `add inserts an account with a liability type`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "Credit Card", accountType = credit)
        repository.add(account)
        assertEquals(listOf(account), repository.accounts.first())
    }

    @Test
    fun `add inserts multiple accounts`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account1 = Account(name = "First Account", accountType = checking)
        val account2 = Account(name = "Second Account", accountType = credit)
        repository.add(account1)
        repository.add(account2)
        val accounts = repository.accounts.first()
        assertEquals(2, accounts.size)
        assertContains(accounts, account1)
        assertContains(accounts, account2)
    }

    @Test
    fun `update modifies name and number of an existing account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Account(name = "Original Name", accountType = checking)
        repository.add(original)
        val updated = original.copy(name = "Updated Name", number = "5678")
        repository.update(updated)
        assertEquals(listOf(updated), repository.accounts.first())
    }

    @Test
    fun `update modifies the type of an existing account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Account(name = "Original Name", accountType = checking)
        repository.add(original)
        val updated = original.copy(accountType = credit)
        repository.update(updated)
        assertEquals(listOf(updated), repository.accounts.first())
    }

    @Test
    fun `update modifies the bank of an existing account`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val bank = Bank(name = "First Bank")
        SqlBankRepository(database, dispatcher).add(bank)
        val repository = SqlAccountRepository(database, dispatcher)
        val original = Account(name = "Checking", accountType = checking)
        repository.add(original)
        val updated = original.copy(bankId = bank.id)
        repository.update(updated)
        assertEquals(listOf(updated), repository.accounts.first())
    }

    @Test
    fun `update modifies the initial balance of an existing account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Account(name = "Checking", accountType = checking)
        repository.add(original)
        val updated = original.copy(initialBalance = Money(500))
        repository.update(updated)
        assertEquals(listOf(updated), repository.accounts.first())
    }

    @Test
    fun `update does not affect other accounts`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account1 = Account(name = "Account One", accountType = checking)
        val account2 = Account(name = "Account Two", accountType = checking)
        repository.add(account1)
        repository.add(account2)
        val updatedAccount1 = account1.copy(name = "Account One Updated")
        repository.update(updatedAccount1)
        val accounts = repository.accounts.first()
        assertContains(accounts, updatedAccount1)
        assertContains(accounts, account2)
    }

    @Test
    fun `removeById removes an account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "To Delete", accountType = checking)
        repository.add(account)
        repository.removeById(account.id)
        assertTrue(repository.accounts.first().isEmpty())
    }

    @Test
    fun `removeById only removes the specified account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account1 = Account(name = "Keep", accountType = checking)
        val account2 = Account(name = "Delete", accountType = checking)
        repository.add(account1)
        repository.add(account2)
        repository.removeById(account2.id)
        assertEquals(listOf(account1), repository.accounts.first())
    }

    @Test
    fun `removeById of unknown ID leaves accounts unchanged`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "Existing", accountType = checking)
        repository.add(account)
        repository.removeById(AccountId())
        assertEquals(listOf(account), repository.accounts.first())
    }

    @Test
    fun `remove deletes an account by reference`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "To Delete", accountType = checking)
        repository.add(account)
        repository.remove(account)
        assertTrue(repository.accounts.first().isEmpty())
    }
}
