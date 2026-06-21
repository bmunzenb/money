package com.munzenberger.money.data.sql.account

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeGroup
import com.munzenberger.money.data.api.account.AccountTypeGroupConstant
import com.munzenberger.money.data.api.account.AccountTypeGroupId
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.account.Statement
import com.munzenberger.money.data.api.account.StatementId
import com.munzenberger.money.data.api.account.remove
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.createTestDatabase
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
class SqlStatementRepositoryTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        group = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private suspend fun createAccount(database: MoneyDatabase, dispatcher: CoroutineDispatcher): AccountId {
        val account = Account(name = "Checking", accountType = checking)
        SqlAccountRepository(database, dispatcher).add(account)
        return account.id
    }

    private fun createRepository(database: MoneyDatabase, context: CoroutineDispatcher): SqlStatementRepository =
        SqlStatementRepository(database, context)

    @Test
    fun `statementsByAccountId emits empty list when no statements exist`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        assertTrue(repository.statementsByAccountId(accountId).first().isEmpty())
    }

    @Test
    fun `add inserts a statement`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        repository.add(statement)
        assertEquals(listOf(statement), repository.statementsByAccountId(accountId).first())
    }

    @Test
    fun `add inserts a reconciled statement`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
            isReconciled = true,
        )
        repository.add(statement)
        assertEquals(listOf(statement), repository.statementsByAccountId(accountId).first())
    }

    @Test
    fun `add inserts multiple statements`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement1 = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        val statement2 = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 2, 15),
            startingBalance = Money(2000),
            endingBalance = Money(3000),
        )
        repository.add(statement1)
        repository.add(statement2)
        val statements = repository.statementsByAccountId(accountId).first()
        assertEquals(2, statements.size)
        assertContains(statements, statement1)
        assertContains(statements, statement2)
    }

    @Test
    fun `statementsByAccountId only returns statements for the specified account`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId1 = createAccount(database, dispatcher)
        val accountId2 = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement1 = Statement(
            accountId = accountId1,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        val statement2 = Statement(
            accountId = accountId2,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(500),
            endingBalance = Money(1500),
        )
        repository.add(statement1)
        repository.add(statement2)
        assertEquals(listOf(statement1), repository.statementsByAccountId(accountId1).first())
    }

    @Test
    fun `update modifies an existing statement`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val original = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        repository.add(original)
        val updated = original.copy(endingBalance = Money(2500), isReconciled = true)
        repository.update(updated)
        assertEquals(listOf(updated), repository.statementsByAccountId(accountId).first())
    }

    @Test
    fun `update does not affect other statements`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement1 = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        val statement2 = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 2, 15),
            startingBalance = Money(2000),
            endingBalance = Money(3000),
        )
        repository.add(statement1)
        repository.add(statement2)
        val updatedStatement1 = statement1.copy(isReconciled = true)
        repository.update(updatedStatement1)
        val statements = repository.statementsByAccountId(accountId).first()
        assertContains(statements, updatedStatement1)
        assertContains(statements, statement2)
    }

    @Test
    fun `removeById removes a statement`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        repository.add(statement)
        repository.removeById(statement.id)
        assertTrue(repository.statementsByAccountId(accountId).first().isEmpty())
    }

    @Test
    fun `removeById only removes the specified statement`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement1 = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        val statement2 = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 2, 15),
            startingBalance = Money(2000),
            endingBalance = Money(3000),
        )
        repository.add(statement1)
        repository.add(statement2)
        repository.removeById(statement2.id)
        assertEquals(listOf(statement1), repository.statementsByAccountId(accountId).first())
    }

    @Test
    fun `removeById of unknown ID leaves statements unchanged`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        repository.add(statement)
        repository.removeById(StatementId())
        assertEquals(listOf(statement), repository.statementsByAccountId(accountId).first())
    }

    @Test
    fun `remove deletes a statement by reference`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val database = createTestDatabase()
        val accountId = createAccount(database, dispatcher)
        val repository = createRepository(database, dispatcher)
        val statement = Statement(
            accountId = accountId,
            closingDate = LocalDate(2024, 1, 15),
            startingBalance = Money(1000),
            endingBalance = Money(2000),
        )
        repository.add(statement)
        repository.remove(statement)
        assertTrue(repository.statementsByAccountId(accountId).first().isEmpty())
    }
}
