package com.munzenberger.money.repository.sql.payee

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.munzenberger.money.repository.api.payee.Payee
import com.munzenberger.money.repository.api.payee.PayeeId
import com.munzenberger.money.repository.api.payee.remove
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class SqlPayeeRepositoryTest {

    private fun createRepository(context: CoroutineDispatcher): SqlPayeeRepository {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MoneyDatabase.Schema.create(driver)
        return SqlPayeeRepository(driver, context)
    }

    @Test
    fun `payees emits empty list when no payees exist`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        assertTrue(repository.payees.first().isEmpty())
    }

    @Test
    fun `add inserts a payee`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val payee = Payee(name = "Grocery Store", memo = null)
        repository.add(payee)
        assertEquals(listOf(payee), repository.payees.first())
    }

    @Test
    fun `add inserts a payee with a memo`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val payee = Payee(name = "Grocery Store", memo = "A memo")
        repository.add(payee)
        assertEquals(listOf(payee), repository.payees.first())
    }

    @Test
    fun `add inserts multiple payees`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val payee1 = Payee(name = "First Payee", memo = null)
        val payee2 = Payee(name = "Second Payee", memo = "memo")
        repository.add(payee1)
        repository.add(payee2)
        val payees = repository.payees.first()
        assertEquals(2, payees.size)
        assertContains(payees, payee1)
        assertContains(payees, payee2)
    }

    @Test
    fun `update modifies name and memo of an existing payee`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Payee(name = "Original Name", memo = null)
        repository.add(original)
        val updated = original.copy(name = "Updated Name", memo = "new memo")
        repository.update(updated)
        assertEquals(listOf(updated), repository.payees.first())
    }

    @Test
    fun `update does not affect other payees`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val payee1 = Payee(name = "Payee One", memo = null)
        val payee2 = Payee(name = "Payee Two", memo = null)
        repository.add(payee1)
        repository.add(payee2)
        val updatedPayee1 = payee1.copy(name = "Payee One Updated")
        repository.update(updatedPayee1)
        val payees = repository.payees.first()
        assertContains(payees, updatedPayee1)
        assertContains(payees, payee2)
    }

    @Test
    fun `removeById removes a payee`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val payee = Payee(name = "To Delete", memo = null)
        repository.add(payee)
        repository.removeById(payee.id)
        assertTrue(repository.payees.first().isEmpty())
    }

    @Test
    fun `removeById only removes the specified payee`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val payee1 = Payee(name = "Keep", memo = null)
        val payee2 = Payee(name = "Delete", memo = null)
        repository.add(payee1)
        repository.add(payee2)
        repository.removeById(payee2.id)
        assertEquals(listOf(payee1), repository.payees.first())
    }

    @Test
    fun `removeById of unknown ID leaves payees unchanged`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val payee = Payee(name = "Existing", memo = null)
        repository.add(payee)
        repository.removeById(PayeeId())
        assertEquals(listOf(payee), repository.payees.first())
    }

    @Test
    fun `remove deletes a payee by reference`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val payee = Payee(name = "To Delete", memo = null)
        repository.add(payee)
        repository.remove(payee)
        assertTrue(repository.payees.first().isEmpty())
    }
}
