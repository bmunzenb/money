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
import com.munzenberger.money.repository.api.category.Category
import com.munzenberger.money.repository.api.category.CategoryId
import com.munzenberger.money.repository.api.category.CategoryType
import com.munzenberger.money.repository.api.category.CategoryTypeConstant
import com.munzenberger.money.repository.api.category.CategoryTypeId
import com.munzenberger.money.repository.api.transaction.CategoryEntry
import com.munzenberger.money.repository.api.transaction.CategoryEntryId
import com.munzenberger.money.repository.api.transaction.Transaction
import com.munzenberger.money.repository.api.transaction.TransactionId
import com.munzenberger.money.repository.api.transaction.TransactionStatus
import com.munzenberger.money.repository.api.transaction.TransactionStatusConstant
import com.munzenberger.money.repository.api.transaction.TransactionStatusId
import com.munzenberger.money.repository.api.transaction.remove
import com.munzenberger.money.repository.sql.MoneyDatabase
import com.munzenberger.money.repository.sql.account.SqlAccountRepository
import com.munzenberger.money.repository.sql.category.SqlCategoryRepository
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
class SqlCategoryEntryRepositoryTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        group = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private val unreconciled = TransactionStatus(id = TransactionStatusId(1), value = TransactionStatusConstant.Unreconciled)

    private val expense = CategoryType(id = CategoryTypeId(2), value = CategoryTypeConstant.Expense)

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

    private suspend fun createCategory(database: MoneyDatabase, dispatcher: CoroutineDispatcher, name: String = "Groceries"): CategoryId {
        val category = Category(name = name, type = expense)
        SqlCategoryRepository(database, dispatcher).add(category)
        return category.id
    }

    private fun createRepository(database: MoneyDatabase, context: CoroutineDispatcher): SqlCategoryEntryRepository =
        SqlCategoryEntryRepository(database, context)

    @Test
    fun `categoryEntriesByTransactionId emits empty list when no category entries exist`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val repository = createRepository(database, dispatcher)
        assertTrue(repository.categoryEntriesByTransactionId(transactionId).first().isEmpty())
    }

    @Test
    fun `categoryEntriesByCategoryId emits empty list when no category entries exist`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        assertTrue(repository.categoryEntriesByCategoryId(categoryId).first().isEmpty())
    }

    @Test
    fun `categoryEntriesByCategoryId returns entries for the specified category`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val categoryEntry = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        repository.add(categoryEntry)
        assertEquals(listOf(categoryEntry), repository.categoryEntriesByCategoryId(categoryId).first())
    }

    @Test
    fun `categoryEntriesByCategoryId only returns entries for the specified category`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId1 = createCategory(database, dispatcher, "Groceries")
        val categoryId2 = createCategory(database, dispatcher, "Rent")
        val repository = createRepository(database, dispatcher)
        val entry1 = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId1,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        val entry2 = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId2,
            amount = Money(-1000),
            orderInTransaction = 1,
        )
        repository.add(entry1)
        repository.add(entry2)
        assertEquals(listOf(entry1), repository.categoryEntriesByCategoryId(categoryId1).first())
    }

    @Test
    fun `add inserts a category entry`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val categoryEntry = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        repository.add(categoryEntry)
        assertEquals(listOf(categoryEntry), repository.categoryEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `add inserts a category entry with a memo`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val categoryEntry = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(-500),
            memo = "Weekly shopping",
            orderInTransaction = 0,
        )
        repository.add(categoryEntry)
        assertEquals(listOf(categoryEntry), repository.categoryEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `add inserts multiple category entries`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val entry1 = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        val entry2 = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(-1000),
            orderInTransaction = 1,
        )
        repository.add(entry1)
        repository.add(entry2)
        val entries = repository.categoryEntriesByTransactionId(transactionId).first()
        assertEquals(2, entries.size)
        assertContains(entries, entry1)
        assertContains(entries, entry2)
    }

    @Test
    fun `categoryEntriesByTransactionId only returns entries for the specified transaction`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId1 = createTransaction(database, dispatcher, accountId)
        val transactionId2 = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val entry1 = CategoryEntry(
            transactionId = transactionId1,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        val entry2 = CategoryEntry(
            transactionId = transactionId2,
            categoryId = categoryId,
            amount = Money(2000),
            orderInTransaction = 0,
        )
        repository.add(entry1)
        repository.add(entry2)
        assertEquals(listOf(entry1), repository.categoryEntriesByTransactionId(transactionId1).first())
    }

    @Test
    fun `update modifies the amount and memo of an existing category entry`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val original = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        repository.add(original)
        val updated = original.copy(amount = Money(2000), memo = "Updated")
        repository.update(updated)
        assertEquals(listOf(updated), repository.categoryEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `update does not affect other category entries`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val entry1 = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        val entry2 = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(2000),
            orderInTransaction = 1,
        )
        repository.add(entry1)
        repository.add(entry2)
        val updatedEntry1 = entry1.copy(memo = "Updated")
        repository.update(updatedEntry1)
        val entries = repository.categoryEntriesByTransactionId(transactionId).first()
        assertContains(entries, updatedEntry1)
        assertContains(entries, entry2)
    }

    @Test
    fun `removeById removes a category entry`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val categoryEntry = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        repository.add(categoryEntry)
        repository.removeById(categoryEntry.id)
        assertTrue(repository.categoryEntriesByTransactionId(transactionId).first().isEmpty())
    }

    @Test
    fun `removeById only removes the specified category entry`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val entry1 = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        val entry2 = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(2000),
            orderInTransaction = 1,
        )
        repository.add(entry1)
        repository.add(entry2)
        repository.removeById(entry2.id)
        assertEquals(listOf(entry1), repository.categoryEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `removeById of unknown ID leaves category entries unchanged`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val categoryEntry = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        repository.add(categoryEntry)
        repository.removeById(CategoryEntryId())
        assertEquals(listOf(categoryEntry), repository.categoryEntriesByTransactionId(transactionId).first())
    }

    @Test
    fun `remove deletes a category entry by reference`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val transactionId = createTransaction(database, dispatcher, accountId)
        val categoryId = createCategory(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val categoryEntry = CategoryEntry(
            transactionId = transactionId,
            categoryId = categoryId,
            amount = Money(1000),
            orderInTransaction = 0,
        )
        repository.add(categoryEntry)
        repository.remove(categoryEntry)
        assertTrue(repository.categoryEntriesByTransactionId(transactionId).first().isEmpty())
    }
}
