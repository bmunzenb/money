package com.munzenberger.money.repository.sqlite

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.munzenberger.money.repository.api.ModelState
import com.munzenberger.money.repository.api.bank.Bank
import com.munzenberger.money.repository.api.bank.BankId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class SqliteBanksRepositoryTest {

    private fun createRepository(context: CoroutineDispatcher): SqliteBanksRepository {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MoneyDatabase.Schema.create(driver)
        return SqliteBanksRepository(driver, context)
    }

    @Test
    fun `banks emits Loading as first state`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        assertIs<ModelState.Loading<List<Bank>>>(repository.banks.first())
    }

    @Test
    fun `banks emits empty list when no banks exist`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertTrue(success.data.isEmpty())
    }

    @Test
    fun `create inserts a bank`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "First Bank", memo = null)
        repository.create(bank)
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertEquals(listOf(bank), success.data)
    }

    @Test
    fun `create inserts a bank with a memo`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "First Bank", memo = "A memo")
        repository.create(bank)
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertEquals(listOf(bank), success.data)
    }

    @Test
    fun `create inserts multiple banks`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank1 = Bank(name = "First Bank", memo = null)
        val bank2 = Bank(name = "Second Bank", memo = "memo")
        repository.create(bank1)
        repository.create(bank2)
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertEquals(2, success.data.size)
        assertContains(success.data, bank1)
        assertContains(success.data, bank2)
    }

    @Test
    fun `update modifies name and memo of an existing bank`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Bank(name = "Original Name", memo = null)
        repository.create(original)
        val updated = original.copy(name = "Updated Name", memo = "new memo")
        repository.update(updated)
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertEquals(listOf(updated), success.data)
    }

    @Test
    fun `update does not affect other banks`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank1 = Bank(name = "Bank One", memo = null)
        val bank2 = Bank(name = "Bank Two", memo = null)
        repository.create(bank1)
        repository.create(bank2)
        val updatedBank1 = bank1.copy(name = "Bank One Updated")
        repository.update(updatedBank1)
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertContains(success.data, updatedBank1)
        assertContains(success.data, bank2)
    }

    @Test
    fun `delete removes a bank by ID`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "To Delete", memo = null)
        repository.create(bank)
        repository.delete(bank.id)
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertTrue(success.data.isEmpty())
    }

    @Test
    fun `delete only removes the specified bank`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank1 = Bank(name = "Keep", memo = null)
        val bank2 = Bank(name = "Delete", memo = null)
        repository.create(bank1)
        repository.create(bank2)
        repository.delete(bank2.id)
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertEquals(listOf(bank1), success.data)
    }

    @Test
    fun `delete of unknown ID leaves banks unchanged`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val bank = Bank(name = "Existing", memo = null)
        repository.create(bank)
        repository.delete(BankId())
        val success = repository.banks.filterIsInstance<ModelState.Success<List<Bank>>>().first()
        assertEquals(listOf(bank), success.data)
    }
}