package com.munzenberger.money.data.sql.account

import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.account.remove
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntry
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.bank.SqlBankRepository
import com.munzenberger.money.data.sql.category.SqlCategoryRepository
import com.munzenberger.money.data.sql.createTestDatabase
import com.munzenberger.money.data.sql.transaction.SqlCategoryEntryRepository
import com.munzenberger.money.data.sql.transaction.SqlTransactionRepository
import com.munzenberger.money.data.sql.transaction.SqlTransferEntryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SqlAccountRepositoryTest {

    private val assets = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets)
    private val liabilities = AccountClass(id = AccountClassId(2), value = AccountClassConstant.Liabilities)

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = assets,
        value = AccountTypeConstant.Checking,
    )
    private val credit = AccountType(
        id = AccountTypeId(5),
        accountClass = liabilities,
        value = AccountTypeConstant.Credit,
    )

    private fun createRepository(context: CoroutineDispatcher): SqlAccountRepository =
        SqlAccountRepository(createTestDatabase(), context)

    private class BalanceFixture(database: MoneyDatabase, dispatcher: CoroutineDispatcher) {
        val repository = SqlAccountRepository(database, dispatcher)
        val categories = SqlCategoryRepository(database, dispatcher)
        val transactions = SqlTransactionRepository(database, dispatcher)
        val categoryEntries = SqlCategoryEntryRepository(database, dispatcher)
        val transferEntries = SqlTransferEntryRepository(database, dispatcher)
        val category = Category(
            name = "Groceries",
            type = CategoryType(id = CategoryTypeId(2), value = CategoryTypeConstant.Expense),
        ).also { categories.add(it) }
    }

    private fun TestScope.createBalanceFixture(): BalanceFixture =
        BalanceFixture(createTestDatabase(), UnconfinedTestDispatcher(testScheduler))

    private fun BalanceFixture.createAccount(name: String, initialBalance: Long = 0): Account =
        Account(name = name, accountType = checking, initialBalance = Money(initialBalance))
            .also { repository.add(it) }

    private fun BalanceFixture.createTransaction(account: Account): Transaction =
        Transaction(
            accountId = account.id,
            date = LocalDate(2024, 1, 15),
            status = TransactionStatus(id = TransactionStatusId(1), value = TransactionStatusConstant.Unreconciled),
        ).also { transactions.add(it) }

    private fun BalanceFixture.addCategoryEntry(transaction: Transaction, amount: Long, order: Int = 0) {
        categoryEntries.add(
            CategoryEntry(
                transactionId = transaction.id,
                categoryId = category.id,
                amount = Money(amount),
                orderInTransaction = order,
            ),
        )
    }

    private fun BalanceFixture.addTransferEntry(transaction: Transaction, to: Account, amount: Long, order: Int = 0) {
        transferEntries.add(
            TransferEntry(
                transactionId = transaction.id,
                accountId = to.id,
                amount = Money(amount),
                status = TransactionStatus(id = TransactionStatusId(1), value = TransactionStatusConstant.Unreconciled),
                orderInTransaction = order,
            ),
        )
    }

    @Test
    fun `accountBalance is the initial balance when the account has no transactions`() = runTest {
        val fixture = createBalanceFixture()
        val account = fixture.createAccount("Checking", initialBalance = 1000)
        assertEquals(Money(1000), fixture.repository.accountBalance(account.id).first())
    }

    @Test
    fun `accountBalance adds the category entries of the account's transactions`() = runTest {
        val fixture = createBalanceFixture()
        val account = fixture.createAccount("Checking", initialBalance = 1000)
        val first = fixture.createTransaction(account)
        fixture.addCategoryEntry(first, -250)
        fixture.addCategoryEntry(first, -50, order = 1)
        fixture.addCategoryEntry(fixture.createTransaction(account), 400)
        assertEquals(Money(1100), fixture.repository.accountBalance(account.id).first())
    }

    @Test
    fun `accountBalance moves a transfer out of the paying account and into the receiving account`() = runTest {
        val fixture = createBalanceFixture()
        val checkingAccount = fixture.createAccount("Checking", initialBalance = 1000)
        val savingsAccount = fixture.createAccount("Savings", initialBalance = 5000)
        // Checking pays 300 into savings: the entry's amount is from checking's view.
        fixture.addTransferEntry(fixture.createTransaction(checkingAccount), to = savingsAccount, amount = -300)
        assertEquals(Money(700), fixture.repository.accountBalance(checkingAccount.id).first())
        assertEquals(Money(5300), fixture.repository.accountBalance(savingsAccount.id).first())
    }

    @Test
    fun `accountBalance combines split transactions, incoming transfers, and the initial balance`() = runTest {
        val fixture = createBalanceFixture()
        val checkingAccount = fixture.createAccount("Checking", initialBalance = 1000)
        val savingsAccount = fixture.createAccount("Savings", initialBalance = 5000)
        // A split from checking: 200 to groceries and 300 to savings.
        val split = fixture.createTransaction(checkingAccount)
        fixture.addCategoryEntry(split, -200)
        fixture.addTransferEntry(split, to = savingsAccount, amount = -300, order = 1)
        // Savings pays 100 back into checking, with 25 of interest income in the same transaction.
        val refund = fixture.createTransaction(savingsAccount)
        fixture.addTransferEntry(refund, to = checkingAccount, amount = -100)
        fixture.addCategoryEntry(refund, 25, order = 1)
        assertEquals(Money(1000 - 200 - 300 + 100), fixture.repository.accountBalance(checkingAccount.id).first())
        assertEquals(Money(5000 + 300 - 100 + 25), fixture.repository.accountBalance(savingsAccount.id).first())
    }

    @Test
    fun `accountBalance ignores other accounts' transactions that don't transfer to it`() = runTest {
        val fixture = createBalanceFixture()
        val checkingAccount = fixture.createAccount("Checking", initialBalance = 1000)
        val savingsAccount = fixture.createAccount("Savings")
        val otherAccount = fixture.createAccount("Other")
        fixture.addCategoryEntry(fixture.createTransaction(savingsAccount), -400)
        fixture.addTransferEntry(fixture.createTransaction(savingsAccount), to = otherAccount, amount = -100)
        assertEquals(Money(1000), fixture.repository.accountBalance(checkingAccount.id).first())
    }

    @Test
    fun `accountBalance emits again when an entry is added or the initial balance changes`() = runTest {
        val fixture = createBalanceFixture()
        val account = fixture.createAccount("Checking", initialBalance = 1000)
        val balances = mutableListOf<Money>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.repository.accountBalance(account.id).toList(balances)
        }
        fixture.addCategoryEntry(fixture.createTransaction(account), -250)
        fixture.repository.update(account.copy(initialBalance = Money(2000)))
        // Any change to a table the query reads re-emits, even when the balance is unchanged.
        assertEquals(listOf(Money(1000), Money(750), Money(1750)), balances.distinct())
    }

    @Test
    fun `accountBalance emits nothing for an unknown account`() = runTest {
        val fixture = createBalanceFixture()
        val balances = mutableListOf<Money>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            fixture.repository.accountBalance(AccountId()).toList(balances)
        }
        assertTrue(balances.isEmpty())
    }

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
    fun `add inserts an account with a memo`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "Checking", accountType = checking, memo = "A memo")
        repository.add(account)
        assertEquals(listOf(account), repository.accounts.first())
    }

    @Test
    fun `add inserts a closed account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "Checking", accountType = checking, isClosed = true)
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
    fun `update modifies the memo of an existing account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Account(name = "Checking", accountType = checking)
        repository.add(original)
        val updated = original.copy(memo = "new memo")
        repository.update(updated)
        assertEquals(listOf(updated), repository.accounts.first())
    }

    @Test
    fun `update closes an existing account`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val account = Account(name = "Checking", accountType = checking)
        repository.add(account)
        val updated = account.copy(isClosed = true)
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
    fun `update of unknown ID throws and leaves accounts unchanged`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Account(name = "Original Name", accountType = checking)
        repository.add(original)
        val updated = original.copy(id = AccountId(), name = "Updated Name", number = "5678")
        assertFailsWith<EntityNotFoundException> { repository.update(updated) }
        assertEquals(listOf(original), repository.accounts.first())
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
