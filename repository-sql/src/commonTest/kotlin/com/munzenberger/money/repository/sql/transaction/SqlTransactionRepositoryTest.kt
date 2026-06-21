package com.munzenberger.money.repository.sql.transaction

import com.munzenberger.money.repository.api.account.Account
import com.munzenberger.money.repository.api.account.AccountId
import com.munzenberger.money.repository.api.account.AccountType
import com.munzenberger.money.repository.api.account.AccountTypeConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroup
import com.munzenberger.money.repository.api.account.AccountTypeGroupConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroupId
import com.munzenberger.money.repository.api.account.AccountTypeId
import com.munzenberger.money.repository.api.payee.Payee
import com.munzenberger.money.repository.api.transaction.Transaction
import com.munzenberger.money.repository.api.transaction.TransactionId
import com.munzenberger.money.repository.api.transaction.TransactionStatus
import com.munzenberger.money.repository.api.transaction.TransactionStatusConstant
import com.munzenberger.money.repository.api.transaction.TransactionStatusId
import com.munzenberger.money.repository.api.transaction.remove
import com.munzenberger.money.repository.sql.MoneyDatabase
import com.munzenberger.money.repository.sql.account.SqlAccountRepository
import com.munzenberger.money.repository.sql.createTestDatabase
import com.munzenberger.money.repository.sql.payee.SqlPayeeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SqlTransactionRepositoryTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        group = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private val unreconciled = TransactionStatus(id = TransactionStatusId(1), value = TransactionStatusConstant.Unreconciled)
    private val cleared = TransactionStatus(id = TransactionStatusId(2), value = TransactionStatusConstant.Cleared)
    private val reconciled = TransactionStatus(id = TransactionStatusId(3), value = TransactionStatusConstant.Reconciled)

    private suspend fun createAccount(database: MoneyDatabase, dispatcher: CoroutineDispatcher): AccountId {
        val account = Account(name = "Checking", accountType = checking)
        SqlAccountRepository(database, dispatcher).add(account)
        return account.id
    }

    private fun createRepository(database: MoneyDatabase, context: CoroutineDispatcher): SqlTransactionRepository =
        SqlTransactionRepository(database, context)

    @Test
    fun `transactionsByAccountId emits empty list when no transactions exist`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        assertTrue(repository.transactionsByAccountId(accountId).first().isEmpty())
    }

    @Test
    fun `add inserts a transaction`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction = Transaction(
            accountId = accountId,
            date = LocalDate(2024, 1, 15),
            memo = "Groceries",
            status = unreconciled,
        )
        repository.add(transaction)
        assertEquals(listOf(transaction), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `add inserts a transaction with a payee`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val payee = Payee(name = "Grocery Store")
        SqlPayeeRepository(database, dispatcher).add(payee)
        val repository = createRepository(database, dispatcher)
        val transaction = Transaction(
            accountId = accountId,
            payeeId = payee.id,
            date = LocalDate(2024, 1, 15),
            status = unreconciled,
        )
        repository.add(transaction)
        assertEquals(listOf(transaction), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `add inserts a transaction with a number`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction = Transaction(
            accountId = accountId,
            date = LocalDate(2024, 1, 15),
            number = "1001",
            status = unreconciled,
        )
        repository.add(transaction)
        assertEquals(listOf(transaction), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `add inserts a transaction with a status`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction = Transaction(
            accountId = accountId,
            date = LocalDate(2024, 1, 15),
            status = reconciled,
        )
        repository.add(transaction)
        assertEquals(listOf(transaction), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `add inserts multiple transactions`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction1 = Transaction(
            accountId = accountId,
            date = LocalDate(2024, 1, 15),
            memo = "First",
            status = unreconciled,
        )
        val transaction2 = Transaction(
            accountId = accountId,
            date = LocalDate(2024, 1, 16),
            memo = "Second",
            status = unreconciled,
        )
        repository.add(transaction1)
        repository.add(transaction2)
        val transactions = repository.transactionsByAccountId(accountId).first()
        assertEquals(2, transactions.size)
        assertContains(transactions, transaction1)
        assertContains(transactions, transaction2)
    }

    @Test
    fun `transactionsByAccountId only returns transactions for the specified account`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId1 = createAccount(database, dispatcher)
        val accountId2 = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction1 = Transaction(accountId = accountId1, date = LocalDate(2024, 1, 15), status = unreconciled)
        val transaction2 = Transaction(accountId = accountId2, date = LocalDate(2024, 1, 16), status = unreconciled)
        repository.add(transaction1)
        repository.add(transaction2)
        assertEquals(listOf(transaction1), repository.transactionsByAccountId(accountId1).first())
    }

    @Test
    fun `update modifies memo and number of an existing transaction`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val original = Transaction(
            accountId = accountId,
            date = LocalDate(2024, 1, 15),
            memo = "Original",
            status = unreconciled,
        )
        repository.add(original)
        val updated = original.copy(memo = "Updated", number = "1002")
        repository.update(updated)
        assertEquals(listOf(updated), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `update modifies the date and status of an existing transaction`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val original = Transaction(accountId = accountId, date = LocalDate(2024, 1, 15), status = unreconciled)
        repository.add(original)
        val updated = original.copy(date = LocalDate(2024, 2, 1), status = cleared)
        repository.update(updated)
        assertEquals(listOf(updated), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `update modifies the payee of an existing transaction`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val payee = Payee(name = "Grocery Store")
        SqlPayeeRepository(database, dispatcher).add(payee)
        val repository = createRepository(database, dispatcher)
        val original = Transaction(accountId = accountId, date = LocalDate(2024, 1, 15), status = unreconciled)
        repository.add(original)
        val updated = original.copy(payeeId = payee.id)
        repository.update(updated)
        assertEquals(listOf(updated), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `update does not affect other transactions`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction1 = Transaction(
            accountId = accountId,
            date = LocalDate(2024, 1, 15),
            memo = "First",
            status = unreconciled,
        )
        val transaction2 = Transaction(
            accountId = accountId,
            date = LocalDate(2024, 1, 16),
            memo = "Second",
            status = unreconciled,
        )
        repository.add(transaction1)
        repository.add(transaction2)
        val updatedTransaction1 = transaction1.copy(memo = "First Updated")
        repository.update(updatedTransaction1)
        val transactions = repository.transactionsByAccountId(accountId).first()
        assertContains(transactions, updatedTransaction1)
        assertContains(transactions, transaction2)
    }

    @Test
    fun `removeById removes a transaction`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction = Transaction(accountId = accountId, date = LocalDate(2024, 1, 15), status = unreconciled)
        repository.add(transaction)
        repository.removeById(transaction.id)
        assertTrue(repository.transactionsByAccountId(accountId).first().isEmpty())
    }

    @Test
    fun `removeById only removes the specified transaction`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction1 = Transaction(accountId = accountId, date = LocalDate(2024, 1, 15), status = unreconciled)
        val transaction2 = Transaction(accountId = accountId, date = LocalDate(2024, 1, 16), status = unreconciled)
        repository.add(transaction1)
        repository.add(transaction2)
        repository.removeById(transaction2.id)
        assertEquals(listOf(transaction1), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `removeById of unknown ID leaves transactions unchanged`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction = Transaction(accountId = accountId, date = LocalDate(2024, 1, 15), status = unreconciled)
        repository.add(transaction)
        repository.removeById(TransactionId())
        assertEquals(listOf(transaction), repository.transactionsByAccountId(accountId).first())
    }

    @Test
    fun `remove deletes a transaction by reference`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val transaction = Transaction(accountId = accountId, date = LocalDate(2024, 1, 15), status = unreconciled)
        repository.add(transaction)
        repository.remove(transaction)
        assertTrue(repository.transactionsByAccountId(accountId).first().isEmpty())
    }
}
