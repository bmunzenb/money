package com.munzenberger.money.repository.sql.transaction

import com.munzenberger.money.repository.api.Money
import com.munzenberger.money.repository.api.account.Account
import com.munzenberger.money.repository.api.account.AccountId
import com.munzenberger.money.repository.api.account.AccountType
import com.munzenberger.money.repository.api.account.AccountTypeConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroup
import com.munzenberger.money.repository.api.account.AccountTypeGroupConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroupId
import com.munzenberger.money.repository.api.account.AccountTypeId
import com.munzenberger.money.repository.api.transaction.Transaction
import com.munzenberger.money.repository.api.transaction.TransactionId
import com.munzenberger.money.repository.api.transaction.TransactionStatus
import com.munzenberger.money.repository.api.transaction.TransactionStatusConstant
import com.munzenberger.money.repository.api.transaction.TransactionStatusId
import com.munzenberger.money.repository.api.transaction.TransferEntry
import com.munzenberger.money.repository.api.transaction.TransferEntryId
import com.munzenberger.money.repository.api.transaction.remove
import com.munzenberger.money.repository.sql.MoneyDatabase
import com.munzenberger.money.repository.sql.account.SqlAccountRepository
import com.munzenberger.money.repository.sql.createTestDatabase
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
class SqlTransferEntryRepositoryTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        group = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private val unreconciled = TransactionStatus(id = TransactionStatusId(1), value = TransactionStatusConstant.Unreconciled)
    private val cleared = TransactionStatus(id = TransactionStatusId(2), value = TransactionStatusConstant.Cleared)

    private suspend fun createAccount(database: MoneyDatabase, dispatcher: CoroutineDispatcher): AccountId {
        val account = Account(name = "Checking", accountType = checking)
        SqlAccountRepository(database, dispatcher).add(account)
        return account.id
    }

    private suspend fun createTransaction(
        database: MoneyDatabase,
        dispatcher: CoroutineDispatcher,
        accountId: AccountId,
    ): TransactionId {
        val transaction = Transaction(accountId = accountId, date = LocalDate(2024, 1, 15), status = unreconciled)
        SqlTransactionRepository(database, dispatcher).add(transaction)
        return transaction.id
    }

    private fun createRepository(database: MoneyDatabase, context: CoroutineDispatcher): SqlTransferEntryRepository =
        SqlTransferEntryRepository(database, context)

    @Test
    fun `transferEntriesByTransactionId emits empty list when no transfer entries exist`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        assertTrue(repository.transferEntriesByTransactionId(transactionId).first().isEmpty())
    }

    @Test
    fun `transferEntriesByAccountId emits empty list when no transfer entries exist`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        assertTrue(repository.transferEntriesByAccountId(accountId).first().isEmpty())
    }

    @Test
    fun `transferEntriesByAccountId returns entries for the specified account`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val transferEntry = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        repository.add(transferEntry)
        assertEquals(listOf(transferEntry), repository.transferEntriesByAccountId(accountId).first())
    }

    @Test
    fun `transferEntriesByAccountId only returns entries for the specified account`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId1 = createAccount(database, dispatcher)
        val accountId2 = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId1)
        val repository = createRepository(database, dispatcher)
        val entry1 = TransferEntry(
            transactionId = transactionId,
            accountId = accountId1,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        val entry2 = TransferEntry(
            transactionId = transactionId,
            accountId = accountId2,
            amount = Money(-1000),
            status = unreconciled,
            orderInTransaction = 1,
        )
        repository.add(entry1)
        repository.add(entry2)
        assertEquals(listOf(entry1), repository.transferEntriesByAccountId(accountId1).first())
    }

    @Test
    fun `add inserts a transfer entry`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val transferEntry = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        repository.add(transferEntry)
        assertEquals(listOf(transferEntry), repository.transferEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `add inserts a transfer entry with a number and memo`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val transferEntry = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(-500),
            number = "1001",
            memo = "Transfer",
            status = unreconciled,
            orderInTransaction = 0,
        )
        repository.add(transferEntry)
        assertEquals(listOf(transferEntry), repository.transferEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `add inserts multiple transfer entries`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val entry1 = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        val entry2 = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(-1000),
            status = unreconciled,
            orderInTransaction = 1,
        )
        repository.add(entry1)
        repository.add(entry2)
        val entries = repository.transferEntriesByTransactionId(transactionId).first()
        assertEquals(2, entries.size)
        assertContains(entries, entry1)
        assertContains(entries, entry2)
    }

    @Test
    fun `transferEntriesByTransactionId only returns entries for the specified transaction`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId1 = createTransaction(database, dispatcher, accountId)
        val transactionId2 = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val entry1 = TransferEntry(
            transactionId = transactionId1,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        val entry2 = TransferEntry(
            transactionId = transactionId2,
            accountId = accountId,
            amount = Money(2000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        repository.add(entry1)
        repository.add(entry2)
        assertEquals(listOf(entry1), repository.transferEntriesByTransactionId(transactionId1).first())
    }

    @Test
    fun `update modifies the amount and status of an existing transfer entry`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val original = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        repository.add(original)
        val updated = original.copy(amount = Money(2000), status = cleared)
        repository.update(updated)
        assertEquals(listOf(updated), repository.transferEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `update does not affect other transfer entries`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val entry1 = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        val entry2 = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(2000),
            status = unreconciled,
            orderInTransaction = 1,
        )
        repository.add(entry1)
        repository.add(entry2)
        val updatedEntry1 = entry1.copy(memo = "Updated")
        repository.update(updatedEntry1)
        val entries = repository.transferEntriesByTransactionId(transactionId).first()
        assertContains(entries, updatedEntry1)
        assertContains(entries, entry2)
    }

    @Test
    fun `removeById removes a transfer entry`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val transferEntry = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        repository.add(transferEntry)
        repository.removeById(transferEntry.id)
        assertTrue(repository.transferEntriesByTransactionId(transactionId).first().isEmpty())
    }

    @Test
    fun `removeById only removes the specified transfer entry`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val entry1 = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        val entry2 = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(2000),
            status = unreconciled,
            orderInTransaction = 1,
        )
        repository.add(entry1)
        repository.add(entry2)
        repository.removeById(entry2.id)
        assertEquals(listOf(entry1), repository.transferEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `removeById of unknown ID leaves transfer entries unchanged`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val transferEntry = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        repository.add(transferEntry)
        repository.removeById(TransferEntryId())
        assertEquals(listOf(transferEntry), repository.transferEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `remove deletes a transfer entry by reference`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        val transferEntry = TransferEntry(
            transactionId = transactionId,
            accountId = accountId,
            amount = Money(1000),
            status = unreconciled,
            orderInTransaction = 0,
        )
        repository.add(transferEntry)
        repository.remove(transferEntry)
        assertTrue(repository.transferEntriesByTransactionId(transactionId).first().isEmpty())
    }
}
