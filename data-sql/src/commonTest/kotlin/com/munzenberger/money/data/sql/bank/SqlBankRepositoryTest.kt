package com.munzenberger.money.data.sql.bank

import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.api.bank.BankId
import com.munzenberger.money.data.api.bank.remove
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
class SqlBankRepositoryTest {

    private fun createRepository(context: CoroutineDispatcher): SqlBankRepository {
        return SqlBankRepository(createTestDatabase(), context)
    }

    @Test
    fun `banks emits empty list when no banks exist`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        assertTrue(repository.banks.first().isEmpty())
    }

    @Test
    fun `add inserts a bank`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "First Bank", memo = null)
        repository.add(bank)
        assertEquals(listOf(bank), repository.banks.first())
    }

    @Test
    fun `add inserts a bank with a memo`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "First Bank", memo = "A memo")
        repository.add(bank)
        assertEquals(listOf(bank), repository.banks.first())
    }

    @Test
    fun `add inserts multiple banks`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank1 = Bank(name = "First Bank", memo = null)
        val bank2 = Bank(name = "Second Bank", memo = "memo")
        repository.add(bank1)
        repository.add(bank2)
        val banks = repository.banks.first()
        assertEquals(2, banks.size)
        assertContains(banks, bank1)
        assertContains(banks, bank2)
    }

    @Test
    fun `update modifies name and memo of an existing bank`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Bank(name = "Original Name", memo = null)
        repository.add(original)
        val updated = original.copy(name = "Updated Name", memo = "new memo")
        repository.update(updated)
        assertEquals(listOf(updated), repository.banks.first())
    }

    @Test
    fun `update does not affect other banks`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank1 = Bank(name = "Bank One", memo = null)
        val bank2 = Bank(name = "Bank Two", memo = null)
        repository.add(bank1)
        repository.add(bank2)
        val updatedBank1 = bank1.copy(name = "Bank One Updated")
        repository.update(updatedBank1)
        val banks = repository.banks.first()
        assertContains(banks, updatedBank1)
        assertContains(banks, bank2)
    }

    @Test
    fun `removeById removes a bank`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "To Delete", memo = null)
        repository.add(bank)
        repository.removeById(bank.id)
        assertTrue(repository.banks.first().isEmpty())
    }

    @Test
    fun `removeById only removes the specified bank`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank1 = Bank(name = "Keep", memo = null)
        val bank2 = Bank(name = "Delete", memo = null)
        repository.add(bank1)
        repository.add(bank2)
        repository.removeById(bank2.id)
        assertEquals(listOf(bank1), repository.banks.first())
    }

    @Test
    fun `removeById of unknown ID leaves banks unchanged`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "Existing", memo = null)
        repository.add(bank)
        repository.removeById(BankId())
        assertEquals(listOf(bank), repository.banks.first())
    }

    @Test
    fun `remove deletes a bank by reference`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "To Delete", memo = null)
        repository.add(bank)
        repository.remove(bank)
        assertTrue(repository.banks.first().isEmpty())
    }
}
