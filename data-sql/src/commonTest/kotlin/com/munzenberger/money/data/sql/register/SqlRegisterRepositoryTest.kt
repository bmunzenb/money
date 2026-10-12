package com.munzenberger.money.data.sql.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntry
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.account.SqlAccountRepository
import com.munzenberger.money.data.sql.category.SqlCategoryRepository
import com.munzenberger.money.data.sql.createTestDatabase
import com.munzenberger.money.data.sql.payee.SqlPayeeRepository
import com.munzenberger.money.data.sql.transaction.SqlCategoryEntryRepository
import com.munzenberger.money.data.sql.transaction.SqlTransactionRepository
import com.munzenberger.money.data.sql.transaction.SqlTransferEntryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SqlRegisterRepositoryTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private val expense = CategoryType(id = CategoryTypeId(2), value = CategoryTypeConstant.Expense)

    private val unreconciled = TransactionStatus(
        id = TransactionStatusId(1),
        value = TransactionStatusConstant.Unreconciled,
    )
    private val cleared = TransactionStatus(id = TransactionStatusId(2), value = TransactionStatusConstant.Cleared)
    private val reconciled = TransactionStatus(
        id = TransactionStatusId(3),
        value = TransactionStatusConstant.Reconciled,
    )

    /** Advances by one millisecond on every call, so transactions are created in the order they're added. */
    private class SteppingClock : Clock {
        private var millis = 1_700_000_000_000

        override fun now(): Instant = Instant.fromEpochMilliseconds(millis++)
    }

    private class Fixture(val database: MoneyDatabase, val dispatcher: CoroutineDispatcher) {
        val accounts = SqlAccountRepository(database, dispatcher)
        val payees = SqlPayeeRepository(database, dispatcher)
        val categories = SqlCategoryRepository(database, dispatcher)
        val transactions = SqlTransactionRepository(database, dispatcher, SteppingClock())
        val categoryEntries = SqlCategoryEntryRepository(database, dispatcher)
        val transferEntries = SqlTransferEntryRepository(database, dispatcher)
        val repository = SqlRegisterRepository(database, dispatcher)
    }

    private fun TestScope.createFixture(): Fixture =
        Fixture(createTestDatabase(), UnconfinedTestDispatcher(testScheduler))

    private fun Fixture.createAccount(name: String = "Checking", initialBalance: Money = Money(0)): Account =
        Account(name = name, accountType = checking, initialBalance = initialBalance).also { accounts.add(it) }

    private fun Fixture.createCategory(name: String, parent: Category? = null): Category =
        Category(name = name, parentId = parent?.id, type = expense).also { categories.add(it) }

    private fun Fixture.createTransaction(
        account: Account,
        date: LocalDate = LocalDate(2024, 1, 15),
        payee: Payee? = null,
        number: String? = null,
        memo: String? = null,
        status: TransactionStatus = unreconciled,
    ): Transaction =
        Transaction(
            accountId = account.id,
            payeeId = payee?.id,
            date = date,
            number = number,
            memo = memo,
            status = status,
        ).also { transactions.add(it) }

    private fun Fixture.addCategoryEntry(
        transaction: Transaction,
        category: Category,
        amount: Long,
        orderInTransaction: Int = 0,
    ): CategoryEntry =
        CategoryEntry(
            transactionId = transaction.id,
            categoryId = category.id,
            amount = Money(amount),
            orderInTransaction = orderInTransaction,
        ).also { categoryEntries.add(it) }

    private fun Fixture.addTransferEntry(
        transaction: Transaction,
        account: Account,
        amount: Long,
        orderInTransaction: Int = 0,
        number: String? = null,
        memo: String? = null,
        status: TransactionStatus = unreconciled,
    ): TransferEntry =
        TransferEntry(
            transactionId = transaction.id,
            accountId = account.id,
            amount = Money(amount),
            number = number,
            memo = memo,
            status = status,
            orderInTransaction = orderInTransaction,
        ).also { transferEntries.add(it) }

    private suspend fun Fixture.register(account: Account): List<RegisterEntry> =
        repository.registerEntriesForAccount(account.id).first()

    @Test
    fun `registerEntriesForAccount emits empty list when the account has no entries`() = runTest {
        val fixture = createFixture()
        val account = fixture.createAccount(initialBalance = Money(1000))
        assertTrue(fixture.register(account).isEmpty())
    }

    @Test
    fun `registerEntriesForAccount emits empty list for an unknown account`() = runTest {
        val fixture = createFixture()
        assertTrue(fixture.repository.registerEntriesForAccount(AccountId()).first().isEmpty())
    }

    @Test
    fun `transaction is mapped with its payee, number, memo and status`() = runTest {
        val fixture = createFixture()
        val account = fixture.createAccount(initialBalance = Money(10_000))
        val payee = Payee(name = "Grocer").also { fixture.payees.add(it) }
        val groceries = fixture.createCategory("Groceries")
        val transaction = fixture.createTransaction(
            account = account,
            payee = payee,
            number = "1001",
            memo = "Weekly shop",
            status = cleared,
        )
        val entry = fixture.addCategoryEntry(transaction, groceries, amount = -2500)

        val expected = RegisterEntry.Transaction(
            transactionId = transaction.id,
            accountId = account.id,
            payeeId = payee.id,
            payeeName = "Grocer",
            date = transaction.date,
            amount = Money(-2500),
            balance = Money(7500),
            number = "1001",
            memo = "Weekly shop",
            status = cleared,
            entries = listOf(
                RegisterEntry.Transaction.Entry.Category(
                    categoryEntryId = entry.id,
                    categoryId = groceries.id,
                    categoryName = "Groceries",
                    parentCategoryName = null,
                    amount = Money(-2500),
                ),
            ),
        )
        assertEquals(listOf<RegisterEntry>(expected), fixture.register(account))
    }

    @Test
    fun `transaction without entries has zero amount and leaves the balance unchanged`() = runTest {
        val fixture = createFixture()
        val account = fixture.createAccount(initialBalance = Money(1000))
        val transaction = fixture.createTransaction(account)

        val entry = fixture.register(account).single() as RegisterEntry.Transaction
        assertEquals(transaction.id, entry.transactionId)
        assertTrue(entry.entries.isEmpty())
        assertEquals(Money(0), entry.amount)
        assertEquals(Money(1000), entry.balance)
    }

    @Test
    fun `category entry includes the name of its immediate parent category`() = runTest {
        val fixture = createFixture()
        val account = fixture.createAccount()
        val auto = fixture.createCategory("Auto")
        val fuel = fixture.createCategory("Fuel", parent = auto)
        val premium = fixture.createCategory("Premium", parent = fuel)
        val transaction = fixture.createTransaction(account)
        fixture.addCategoryEntry(transaction, fuel, amount = -100, orderInTransaction = 0)
        fixture.addCategoryEntry(transaction, premium, amount = -200, orderInTransaction = 1)

        val entries = (fixture.register(account).single() as RegisterEntry.Transaction).entries
            .map { it as RegisterEntry.Transaction.Entry.Category }
        assertEquals(
            listOf("Fuel" to "Auto", "Premium" to "Fuel"),
            entries.map { it.categoryName to it.parentCategoryName },
        )
    }

    @Test
    fun `transaction entries are interleaved by order in transaction`() = runTest {
        val fixture = createFixture()
        val account = fixture.createAccount(initialBalance = Money(10_000))
        val savings = fixture.createAccount(name = "Savings")
        val groceries = fixture.createCategory("Groceries")
        val dining = fixture.createCategory("Dining")
        val transaction = fixture.createTransaction(account)
        val second = fixture.addCategoryEntry(transaction, dining, amount = -300, orderInTransaction = 2)
        val transfer = fixture.addTransferEntry(transaction, savings, amount = -1000, orderInTransaction = 1)
        val first = fixture.addCategoryEntry(transaction, groceries, amount = -200, orderInTransaction = 0)

        val entry = fixture.register(account).single() as RegisterEntry.Transaction
        assertEquals(
            listOf(
                RegisterEntry.Transaction.Entry.Category(
                    categoryEntryId = first.id,
                    categoryId = groceries.id,
                    categoryName = "Groceries",
                    parentCategoryName = null,
                    amount = Money(-200),
                ),
                RegisterEntry.Transaction.Entry.Transfer(
                    transferEntryId = transfer.id,
                    accountId = savings.id,
                    accountName = "Savings",
                    amount = Money(-1000),
                ),
                RegisterEntry.Transaction.Entry.Category(
                    categoryEntryId = second.id,
                    categoryId = dining.id,
                    categoryName = "Dining",
                    parentCategoryName = null,
                    amount = Money(-300),
                ),
            ),
            entry.entries,
        )
        assertEquals(Money(-1500), entry.amount)
        assertEquals(Money(8500), entry.balance)
    }

    @Test
    fun `transfer into the account is negated and takes its number, memo and status from the transfer entry`() =
        runTest {
            val fixture = createFixture()
            val checkingAccount = fixture.createAccount(name = "Checking")
            val savings = fixture.createAccount(name = "Savings", initialBalance = Money(5000))
            val payee = Payee(name = "Bank").also { fixture.payees.add(it) }
            val transaction = fixture.createTransaction(
                account = checkingAccount,
                date = LocalDate(2024, 2, 1),
                payee = payee,
                number = "TXN",
                memo = "Transaction memo",
                status = reconciled,
            )
            val transfer = fixture.addTransferEntry(
                transaction = transaction,
                account = savings,
                amount = -1000,
                number = "XFER",
                memo = "Transfer memo",
                status = cleared,
            )

            val expected = RegisterEntry.Transfer(
                transferEntryId = transfer.id,
                accountId = savings.id,
                payeeId = payee.id,
                payeeName = "Bank",
                date = LocalDate(2024, 2, 1),
                amount = Money(1000),
                balance = Money(6000),
                number = "XFER",
                memo = "Transfer memo",
                status = cleared,
            )
            assertEquals(listOf<RegisterEntry>(expected), fixture.register(savings))
        }

    @Test
    fun `entries are ordered by date then by creation and accumulate a running balance`() = runTest {
        val fixture = createFixture()
        val account = fixture.createAccount(initialBalance = Money(1000))
        val savings = fixture.createAccount(name = "Savings")
        val category = fixture.createCategory("Misc")

        val march = fixture.createTransaction(account, date = LocalDate(2024, 3, 1))
        fixture.addCategoryEntry(march, category, amount = -400)
        val januarySecond = fixture.createTransaction(account, date = LocalDate(2024, 1, 1))
        fixture.addCategoryEntry(januarySecond, category, amount = 200)
        // Created after januarySecond, on a later date than it, but registered in savings.
        val februaryTransfer = fixture.createTransaction(savings, date = LocalDate(2024, 2, 1))
        val transfer = fixture.addTransferEntry(februaryTransfer, account, amount = -300)
        val januaryThird = fixture.createTransaction(account, date = LocalDate(2024, 1, 1))
        fixture.addCategoryEntry(januaryThird, category, amount = -50)

        val register = fixture.register(account)
        assertEquals(
            listOf<Any>(januarySecond.id, januaryThird.id, transfer.id, march.id),
            register.map {
                when (it) {
                    is RegisterEntry.Transaction -> it.transactionId
                    is RegisterEntry.Transfer -> it.transferEntryId
                }
            },
        )
        assertEquals(
            listOf(Money(1200), Money(1150), Money(1450), Money(1050)),
            register.map { it.balance },
        )
    }

    @Test
    fun `register only includes entries for the given account`() = runTest {
        val fixture = createFixture()
        val account = fixture.createAccount()
        val other = fixture.createAccount(name = "Other")
        val category = fixture.createCategory("Misc")
        val transaction = fixture.createTransaction(account)
        fixture.addCategoryEntry(transaction, category, amount = -100)
        val otherTransaction = fixture.createTransaction(other)
        fixture.addCategoryEntry(otherTransaction, category, amount = -200)

        val register = fixture.register(account)
        assertEquals(listOf(transaction.id), register.map { (it as RegisterEntry.Transaction).transactionId })
        assertEquals(Money(-100), register.single().amount)
    }

    @Test
    fun `register reflects later changes to entries and denormalized names`() = runTest {
        val fixture = createFixture()
        val account = fixture.createAccount()
        val payee = Payee(name = "Grocer").also { fixture.payees.add(it) }
        val category = fixture.createCategory("Groceries")
        val transaction = fixture.createTransaction(account, payee = payee)
        val flow = fixture.repository.registerEntriesForAccount(account.id)

        fixture.addCategoryEntry(transaction, category, amount = -100)
        assertEquals(Money(-100), flow.first().single().balance)

        fixture.payees.update(payee.copy(name = "Market"))
        fixture.categories.update(category.copy(name = "Food"))
        val entry = flow.first().single() as RegisterEntry.Transaction
        assertEquals("Market", entry.payeeName)
        assertEquals("Food", (entry.entries.single() as RegisterEntry.Transaction.Entry.Category).categoryName)
    }
}
