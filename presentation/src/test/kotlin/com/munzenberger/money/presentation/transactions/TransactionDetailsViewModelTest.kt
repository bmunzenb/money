package com.munzenberger.money.presentation.transactions

import com.munzenberger.money.core.category.CategoryWithParent
import com.munzenberger.money.core.category.GetCategoriesUseCase
import com.munzenberger.money.core.transaction.GetTransactionUseCase
import com.munzenberger.money.core.transaction.TransactionWithEntries
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntry
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.loadedOrNull
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
// One test class for the whole form, so its fixtures are shared; it grows with the form's fields.
@Suppress("LargeClass")
class TransactionDetailsViewModelTest {

    private val navigator = Navigator()
    private val fixture = MoneyRepositoryControllerFixture()

    // The real use case, a thin read over the repository mock, so these tests cover how the form treats a missing or
    // unreadable transaction without restating the use case's mapping in a mock.
    private val getTransaction = GetTransactionUseCase()
    private val getCategories = GetCategoriesUseCase()

    // Formats a category by its name, or a transfer by its account's.
    private val categoryFormatter = TransactionCategoryFormatter { category ->
        when (category) {
            is TransactionCategory.Category -> category.category.category.name
            is TransactionCategory.Transfer -> category.account.name
        }
    }

    private val today = LocalDate(2026, 5, 15)

    private fun viewModel(route: Route.TransactionFormRoute = Route.NewTransaction()) =
        TransactionDetailsViewModel(
            route,
            fixture.controller,
            navigator,
            getTransaction,
            getCategories,
            categoryFormatter,
            today = { today },
        )

    private val unreconciled = TransactionStatus(TransactionStatusId(1), TransactionStatusConstant.Unreconciled)
    private val reconciled = TransactionStatus(TransactionStatusId(3), TransactionStatusConstant.Reconciled)

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Asset),
        value = AccountTypeConstant.Checking,
    )

    private val everyday = Account(name = "everyday", accountType = checking)
    private val bills = Account(name = "Bills", accountType = checking)

    private val expense = CategoryType(id = CategoryTypeId(2), value = CategoryTypeConstant.Expense)

    private val utilities = Category(name = "utilities", type = expense)
    private val water = Category(name = "Water", parentId = utilities.id, type = expense)
    private val electric = Category(name = "electric", parentId = utilities.id, type = expense)
    private val food = Category(name = "Food", type = expense)

    private val grocer = Payee(name = "grocer")
    private val bakery = Payee(name = "Bakery")

    private val existingTransaction =
        Transaction(
            accountId = bills.id,
            payeeId = grocer.id,
            status = unreconciled,
            date = LocalDate(2026, 1, 20),
            number = "1042",
        )

    private val groceries = CategoryEntry(
        transactionId = existingTransaction.id,
        categoryId = CategoryId(),
        amount = Money(-40),
        orderInTransaction = 0,
    )

    private fun repository(
        transaction: Flow<Transaction?> = flowOf(existingTransaction),
        categoryEntries: Flow<List<CategoryEntry>> = flowOf(listOf(groceries)),
        transferEntries: Flow<List<TransferEntry>> = flowOf(emptyList()),
        accounts: Flow<List<Account>> = flowOf(emptyList()),
        categories: Flow<List<Category>> = flowOf(emptyList()),
        payees: Flow<List<Payee>> = flowOf(emptyList()),
    ) = mockk<MoneyRepository>(relaxUnitFun = true) {
        every { this@mockk.accounts } returns accounts
        every { this@mockk.categories } returns categories
        every { this@mockk.payees } returns payees
        every { transactionById(existingTransaction.id) } returns transaction
        every { categoryEntriesByTransactionId(existingTransaction.id) } returns categoryEntries
        every { transferEntriesByTransactionId(existingTransaction.id) } returns transferEntries
    }

    private fun <T> failingFlow(): Flow<T> = flow { error("query failed") }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a new transaction isn't editing an existing one`() {
        val viewModel = viewModel()

        assertNull(viewModel.state.value.existingTransaction)
        assertFalse(viewModel.state.value.isEditing)
        assertTrue(viewModel.state.value.isFormReady)
    }

    @Test
    fun `an existing transaction is loading until a repository is connected`() {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        assertEquals(LoadState.Loading, viewModel.state.value.existingTransaction)
        assertTrue(viewModel.state.value.isEditing)
        assertFalse(viewModel.state.value.isFormReady)
    }

    @Test
    fun `an existing transaction is loaded with its entries`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository())

        assertEquals(
            LoadState.Loaded(TransactionWithEntries(existingTransaction, listOf(groceries))),
            viewModel.state.value.existingTransaction,
        )
        assertTrue(viewModel.state.value.isFormReady)
    }

    @Test
    fun `an existing transaction is read only once`() = runTest {
        val transaction = MutableStateFlow<Transaction?>(existingTransaction)
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        fixture.connect(repository(transaction = transaction))

        transaction.value = existingTransaction.copy(memo = "Changed elsewhere.")

        assertEquals(
            LoadState.Loaded(TransactionWithEntries(existingTransaction, listOf(groceries))),
            viewModel.state.value.existingTransaction,
        )
    }

    @Test
    fun `an existing transaction is an error when it isn't found`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(transaction = flowOf(null)))

        assertEquals(LoadState.Error, viewModel.state.value.existingTransaction)
    }

    @Test
    fun `an existing transaction is an error when it can't be loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(categoryEntries = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.existingTransaction)
    }

    @Test
    fun `a new transaction starts with one empty entry`() {
        val viewModel = viewModel()

        val entry = viewModel.state.value.entries.loadedOrNull!!.single()
        assertEquals(TransactionEntryInput(key = entry.key), entry)
    }

    @Test
    fun `an existing transaction's entries are loading until a repository is connected`() {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        assertEquals(LoadState.Loading, viewModel.state.value.entries)
    }

    @Test
    fun `an existing transaction's entries are filled in from its saved entries, in order`() = runTest {
        val transfer = TransferEntry(
            transactionId = existingTransaction.id,
            accountId = everyday.id,
            amount = Money(-1000),
            number = "7",
            memo = "Move",
            status = reconciled,
            orderInTransaction = 0,
        )
        val lunch = groceries.copy(categoryId = food.id, memo = "Lunch", orderInTransaction = 1)
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(
            repository(
                categoryEntries = flowOf(listOf(lunch)),
                transferEntries = flowOf(listOf(transfer)),
                accounts = flowOf(listOf(everyday, bills)),
                categories = flowOf(listOf(food)),
            )
        )

        val entries = viewModel.state.value.entries.loadedOrNull!!
        assertEquals(
            listOf(
                TransactionEntryInput(
                    key = entries[0].key,
                    categoryText = everyday.name,
                    category = TransactionCategory.Transfer(everyday),
                    amountText = Money(1000).toString(asCurrency = false),
                    memo = "Move",
                    transferNumber = "7",
                    transferStatus = reconciled,
                ),
                TransactionEntryInput(
                    key = entries[1].key,
                    categoryText = food.name,
                    category = TransactionCategory.Category(CategoryWithParent(food, parent = null)),
                    amountText = Money(40).toString(asCurrency = false),
                    memo = "Lunch",
                ),
            ),
            entries,
        )
    }

    @Test
    fun `an existing transaction's entry against its type has a negative amount`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(
            repository(
                categoryEntries = flowOf(
                    listOf(groceries, groceries.copy(amount = Money(10), orderInTransaction = 1)),
                ),
            )
        )

        assertEquals(
            listOf(Money(40), Money(-10)).map { it.toString(asCurrency = false) },
            viewModel.state.value.entries.loadedOrNull!!.map { it.amountText },
        )
    }

    @Test
    fun `an existing transaction's entry without its category has none selected`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(categories = flowOf(emptyList())))

        assertNull(viewModel.state.value.entries.loadedOrNull!!.single().category)
    }

    @Test
    fun `an existing transaction is an error when the categories can't be loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(categories = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.existingTransaction)
        assertEquals(LoadState.Error, viewModel.state.value.entries)
    }

    @Test
    fun `an existing transaction is an error when the accounts can't be loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(accounts = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.existingTransaction)
        assertEquals(LoadState.Error, viewModel.state.value.entries)
    }

    @Test
    fun `accounts are loading until a repository is connected`() {
        val viewModel = viewModel()

        assertEquals(LoadState.Loading, viewModel.state.value.accounts)
    }

    @Test
    fun `accounts are loaded from the connected repository sorted by name`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(accounts = flowOf(listOf(everyday, bills))))

        assertEquals(LoadState.Loaded(listOf(bills, everyday)), viewModel.state.value.accounts)
    }

    @Test
    fun `accounts are reloaded when they change`() = runTest {
        val accounts = MutableStateFlow(listOf(everyday))
        val viewModel = viewModel()
        fixture.connect(repository(accounts = accounts))

        accounts.value = listOf(everyday, bills)

        assertEquals(LoadState.Loaded(listOf(bills, everyday)), viewModel.state.value.accounts)
    }

    @Test
    fun `accounts are an error when they can't be loaded`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(accounts = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.accounts)
    }

    @Test
    fun `categories are loading until a repository is connected`() {
        val viewModel = viewModel()

        assertEquals(LoadState.Loading, viewModel.state.value.categories)
    }

    @Test
    fun `categories are each category after its parent, then each account, sorted by name`() = runTest {
        val viewModel = viewModel()

        fixture.connect(
            repository(
                accounts = flowOf(listOf(everyday, bills)),
                categories = flowOf(listOf(water, utilities, food, electric)),
            )
        )

        val expected = listOf(
            TransactionCategory.Category(CategoryWithParent(food, parent = null)),
            TransactionCategory.Category(CategoryWithParent(utilities, parent = null)),
            TransactionCategory.Category(CategoryWithParent(electric, parent = utilities)),
            TransactionCategory.Category(CategoryWithParent(water, parent = utilities)),
            TransactionCategory.Transfer(bills),
            TransactionCategory.Transfer(everyday),
        )
        assertEquals(LoadState.Loaded(expected), viewModel.state.value.categories)
    }

    @Test
    fun `categories are reloaded when the accounts change`() = runTest {
        val accounts = MutableStateFlow(listOf(everyday))
        val viewModel = viewModel()
        fixture.connect(repository(accounts = accounts, categories = flowOf(listOf(food))))

        accounts.value = listOf(everyday, bills)

        val expected = listOf(
            TransactionCategory.Category(CategoryWithParent(food, parent = null)),
            TransactionCategory.Transfer(bills),
            TransactionCategory.Transfer(everyday),
        )
        assertEquals(LoadState.Loaded(expected), viewModel.state.value.categories)
    }

    @Test
    fun `categories are reloaded when the categories change`() = runTest {
        val categories = MutableStateFlow(listOf(food))
        val viewModel = viewModel()
        fixture.connect(repository(accounts = flowOf(listOf(bills)), categories = categories))

        categories.value = listOf(food, utilities)

        val expected = listOf(
            TransactionCategory.Category(CategoryWithParent(food, parent = null)),
            TransactionCategory.Category(CategoryWithParent(utilities, parent = null)),
            TransactionCategory.Transfer(bills),
        )
        assertEquals(LoadState.Loaded(expected), viewModel.state.value.categories)
    }

    @Test
    fun `the accounts are read once for both the accounts and the categories`() = runTest {
        val repository = repository(accounts = flowOf(listOf(bills)), categories = flowOf(listOf(food)))
        val viewModel = viewModel()

        fixture.connect(repository)

        assertEquals(LoadState.Loaded(listOf(bills)), viewModel.state.value.accounts)
        assertTrue(viewModel.state.value.categories is LoadState.Loaded)
        verify(exactly = 1) { repository.accounts }
    }

    @Test
    fun `categories are an error when the categories can't be loaded`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(accounts = flowOf(listOf(bills)), categories = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.categories)
    }

    @Test
    fun `categories are an error when the accounts can't be loaded`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(accounts = failingFlow(), categories = flowOf(listOf(food))))

        assertEquals(LoadState.Error, viewModel.state.value.categories)
    }

    @Test
    fun `payees are loading until a repository is connected`() {
        val viewModel = viewModel()

        assertEquals(LoadState.Loading, viewModel.state.value.payees)
    }

    @Test
    fun `payees are loaded from the connected repository sorted by name`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(payees = flowOf(listOf(grocer, bakery))))

        assertEquals(LoadState.Loaded(listOf(bakery, grocer)), viewModel.state.value.payees)
    }

    @Test
    fun `payees are reloaded when they change`() = runTest {
        val payees = MutableStateFlow(listOf(grocer))
        val viewModel = viewModel()
        fixture.connect(repository(payees = payees))

        payees.value = listOf(grocer, bakery)

        assertEquals(LoadState.Loaded(listOf(bakery, grocer)), viewModel.state.value.payees)
    }

    @Test
    fun `payees are an error when they can't be loaded, and a typed name is still remembered`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(payees = failingFlow()))

        viewModel.onPayeeNameChange("New Payee")

        assertEquals(LoadState.Error, viewModel.state.value.payees)
        assertEquals("New Payee", viewModel.state.value.payeeName)
        assertNull(viewModel.state.value.payee)
    }

    @Test
    fun `a new transaction has no payee`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(payees = flowOf(listOf(grocer, bakery))))

        assertEquals("", viewModel.state.value.payeeName)
        assertNull(viewModel.state.value.payee)
    }

    @Test
    fun `an existing transaction's payee is filled in once it's loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(payees = flowOf(listOf(grocer, bakery))))

        assertEquals("grocer", viewModel.state.value.payeeName)
        assertEquals(grocer, viewModel.state.value.payee)
    }

    @Test
    fun `an existing transaction is loading until the payees are loaded`() = runTest {
        val payees = MutableSharedFlow<List<Payee>>()
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        fixture.connect(repository(payees = payees))

        assertEquals(LoadState.Loading, viewModel.state.value.existingTransaction)

        payees.emit(listOf(grocer, bakery))

        assertTrue(viewModel.state.value.isFormReady)
        assertEquals(grocer, viewModel.state.value.payee)
    }

    @Test
    fun `an existing transaction is an error when the payees can't be loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(payees = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.existingTransaction)
    }

    @Test
    fun `an existing transaction without a payee has an empty payee name`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(
            repository(
                transaction = flowOf(existingTransaction.copy(payeeId = null)),
                payees = flowOf(listOf(grocer, bakery)),
            )
        )

        assertEquals("", viewModel.state.value.payeeName)
        assertNull(viewModel.state.value.payee)
    }

    @Test
    fun `an existing transaction that can't be loaded keeps the payee name`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        viewModel.onPayeeNameChange("New Payee")

        fixture.connect(repository(transaction = flowOf(null), payees = flowOf(listOf(grocer, bakery))))

        assertEquals("New Payee", viewModel.state.value.payeeName)
        assertNull(viewModel.state.value.payee)
    }

    @Test
    fun `onPayeeChange selects the existing payee`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(payees = flowOf(listOf(grocer, bakery))))

        viewModel.onPayeeChange(bakery)

        assertEquals("Bakery", viewModel.state.value.payeeName)
        assertEquals(bakery, viewModel.state.value.payee)
    }

    @Test
    fun `onPayeeNameChange with a new name remembers the name without a payee`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(payees = flowOf(listOf(grocer))))

        viewModel.onPayeeNameChange("New Payee")

        assertEquals("New Payee", viewModel.state.value.payeeName)
        assertNull(viewModel.state.value.payee)
    }

    @Test
    fun `onPayeeNameChange with an existing payee's name selects that payee`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(payees = flowOf(listOf(grocer))))

        viewModel.onPayeeNameChange(" Grocer ")

        assertEquals(" Grocer ", viewModel.state.value.payeeName)
        assertEquals(grocer, viewModel.state.value.payee)
    }

    @Test
    fun `editing a selected payee's name clears the selection`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(payees = flowOf(listOf(grocer))))
        viewModel.onPayeeChange(grocer)

        viewModel.onPayeeNameChange("grocer outlet")

        assertNull(viewModel.state.value.payee)
    }

    @Test
    fun `a typed payee name selects its payee once the payees load`() = runTest {
        val payees = MutableSharedFlow<List<Payee>>()
        val viewModel = viewModel()
        fixture.connect(repository(payees = payees))
        viewModel.onPayeeNameChange("bakery")

        payees.emit(listOf(grocer, bakery))

        assertEquals(bakery, viewModel.state.value.payee)
    }

    @Test
    fun `clearing the payee name keeps it cleared when the payees reload`() = runTest {
        val payees = MutableStateFlow(listOf(grocer, bakery))
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        fixture.connect(repository(payees = payees))

        viewModel.onPayeeNameChange("")
        payees.value = listOf(bakery, grocer)

        assertEquals("", viewModel.state.value.payeeName)
        assertNull(viewModel.state.value.payee)
    }

    @Test
    fun `account is initially empty`() {
        val viewModel = viewModel()

        assertNull(viewModel.state.value.account)
    }

    @Test
    fun `onAccountChange selects the account`() {
        val viewModel = viewModel()

        viewModel.onAccountChange(bills)

        assertEquals(bills, viewModel.state.value.account)
    }

    @Test
    fun `a new transaction in an account selects that account once the accounts are loaded`() = runTest {
        val viewModel = viewModel(Route.NewTransaction(bills.id))

        assertNull(viewModel.state.value.account)

        fixture.connect(repository(accounts = flowOf(listOf(everyday, bills))))

        assertEquals(bills, viewModel.state.value.account)
    }

    @Test
    fun `a new transaction without an account selects none`() = runTest {
        val viewModel = viewModel(Route.NewTransaction())

        fixture.connect(repository(accounts = flowOf(listOf(everyday, bills))))

        assertNull(viewModel.state.value.account)
    }

    @Test
    fun `reloading the accounts keeps the selected account`() = runTest {
        val accounts = MutableStateFlow(listOf(everyday, bills))
        val viewModel = viewModel(Route.NewTransaction(bills.id))
        fixture.connect(repository(accounts = accounts))

        viewModel.onAccountChange(everyday)
        accounts.value = listOf(bills, everyday)

        assertEquals(everyday, viewModel.state.value.account)
    }

    @Test
    fun `an existing transaction selects its account once it and the accounts are loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(accounts = flowOf(listOf(everyday, bills))))

        assertEquals(bills, viewModel.state.value.account)
    }

    @Test
    fun `an existing transaction selects its account when the accounts load after it`() = runTest {
        val accounts = MutableSharedFlow<List<Account>>()
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        fixture.connect(repository(accounts = accounts))

        assertNull(viewModel.state.value.account)

        accounts.emit(listOf(everyday, bills))

        assertEquals(bills, viewModel.state.value.account)
    }

    @Test
    fun `an existing transaction that can't be loaded selects no account`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(transaction = flowOf(null), accounts = flowOf(listOf(everyday, bills))))

        assertNull(viewModel.state.value.account)
    }

    @Test
    fun `a new transaction's date is today`() {
        val viewModel = viewModel()

        assertEquals(today, viewModel.state.value.date)
    }

    @Test
    fun `an existing transaction's date is its saved date once it's loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository())

        assertEquals(existingTransaction.date, viewModel.state.value.date)
    }

    @Test
    fun `an existing transaction that can't be loaded keeps today's date`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(transaction = flowOf(null)))

        assertEquals(today, viewModel.state.value.date)
    }

    @Test
    fun `onDateChange sets the date`() {
        val viewModel = viewModel()

        viewModel.onDateChange(LocalDate(2026, 3, 1))

        assertEquals(LocalDate(2026, 3, 1), viewModel.state.value.date)
    }

    @Test
    fun `a new transaction is a decrease`() {
        val viewModel = viewModel()

        assertEquals(TransactionType.Decrease, viewModel.state.value.type)
    }

    @Test
    fun `an existing transaction that lowers the balance is a decrease once it's loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        viewModel.onTypeChange(TransactionType.Increase)

        fixture.connect(repository())

        assertEquals(TransactionType.Decrease, viewModel.state.value.type)
    }

    @Test
    fun `an existing transaction that raises the balance is an increase once it's loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(categoryEntries = flowOf(listOf(groceries, groceries.copy(amount = Money(100))))))

        assertEquals(TransactionType.Increase, viewModel.state.value.type)
    }

    @Test
    fun `an existing transaction of zero is an increase once it's loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(categoryEntries = flowOf(listOf(groceries.copy(amount = Money(0))))))

        assertEquals(TransactionType.Increase, viewModel.state.value.type)
    }

    @Test
    fun `an existing transaction that can't be loaded keeps the type`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(transaction = flowOf(null)))

        assertEquals(TransactionType.Decrease, viewModel.state.value.type)
    }

    @Test
    fun `onTypeChange sets the type`() {
        val viewModel = viewModel()

        viewModel.onTypeChange(TransactionType.Increase)

        assertEquals(TransactionType.Increase, viewModel.state.value.type)
    }

    @Test
    fun `a new transaction's number is empty`() {
        val viewModel = viewModel()

        assertEquals("", viewModel.state.value.number)
    }

    @Test
    fun `an existing transaction's number is its saved number once it's loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository())

        assertEquals("1042", viewModel.state.value.number)
    }

    @Test
    fun `an existing transaction without a number has an empty number once it's loaded`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        viewModel.onNumberChange("7")

        fixture.connect(repository(transaction = flowOf(existingTransaction.copy(number = null))))

        assertEquals("", viewModel.state.value.number)
    }

    @Test
    fun `an existing transaction that can't be loaded keeps the number`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        viewModel.onNumberChange("7")

        fixture.connect(repository(transaction = flowOf(null)))

        assertEquals("7", viewModel.state.value.number)
    }

    @Test
    fun `onNumberChange sets the number`() {
        val viewModel = viewModel()

        viewModel.onNumberChange("1043")

        assertEquals("1043", viewModel.state.value.number)
    }

    @Test
    fun `onBackClick pops the transaction's route from the back stack`() {
        val route = Route.TransactionDetails(existingTransaction.id)
        navigator.navigate { clear(); add(Route.AccountRegister(existingTransaction.accountId)); add(route) }
        val viewModel = viewModel(route)

        viewModel.onBackClick()

        assertEquals(listOf(Route.AccountRegister(existingTransaction.accountId)), navigator.backStack.value)
    }

    @Test
    fun `onEntryCategoryChange selects the category with that text`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(categories = flowOf(listOf(utilities, food))))
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key

        viewModel.onEntryCategoryChange(key, " FOOD ")

        assertEquals(
            TransactionCategory.Category(CategoryWithParent(food, parent = null)),
            viewModel.state.value.entries.loadedOrNull!!.single().category,
        )
    }

    @Test
    fun `onEntryCategoryChange selects the transfer account with that text`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(accounts = flowOf(listOf(everyday, bills)), categories = flowOf(listOf(food))))
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key

        viewModel.onEntryCategoryChange(key, "bills")

        assertEquals(
            TransactionCategory.Transfer(bills),
            viewModel.state.value.entries.loadedOrNull!!.single().category,
        )
    }

    @Test
    fun `onEntryCategoryChange with text that isn't a category clears the category`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(categories = flowOf(listOf(food))))
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key
        viewModel.onEntryCategoryChange(key, "Food")

        viewModel.onEntryCategoryChange(key, "Foo")

        assertNull(viewModel.state.value.entries.loadedOrNull!!.single().category)
    }

    @Test
    fun `onEntryCategoryChange keeps the text as typed`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(categories = flowOf(listOf(food))))
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key

        viewModel.onEntryCategoryChange(key, " FOOD ")
        assertEquals(" FOOD ", viewModel.state.value.entries.loadedOrNull!!.single().categoryText)

        viewModel.onEntryCategoryChange(key, "Gifts")
        assertEquals("Gifts", viewModel.state.value.entries.loadedOrNull!!.single().categoryText)
    }

    @Test
    fun `an existing transaction's entry whose category isn't found has no category text`() = runTest {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        fixture.connect(repository(categoryEntries = flowOf(listOf(groceries)), categories = flowOf(emptyList())))

        val entry = viewModel.state.value.entries.loadedOrNull!!.single()
        assertNull(entry.category)
        assertEquals("", entry.categoryText)
    }

    @Test
    fun `onEntryAmountChange sets the entry's amount text`() {
        val viewModel = viewModel()
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key

        viewModel.onEntryAmountChange(key, "12.5")

        assertEquals("12.5", viewModel.state.value.entries.loadedOrNull!!.single().amountText)
    }

    @Test
    fun `onEntryAmountFocusLost reformats a valid amount`() {
        val viewModel = viewModel()
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key
        viewModel.onEntryAmountChange(key, "1234.5")

        viewModel.onEntryAmountFocusLost(key)

        val entry = viewModel.state.value.entries.loadedOrNull!!.single()
        assertEquals(Money(123450).toString(asCurrency = false), entry.amountText)
        assertFalse(entry.isAmountError)
    }

    @Test
    fun `onEntryAmountFocusLost shows an error for an invalid amount and keeps the text`() {
        val viewModel = viewModel()
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key
        viewModel.onEntryAmountChange(key, "lots")

        viewModel.onEntryAmountFocusLost(key)

        val entry = viewModel.state.value.entries.loadedOrNull!!.single()
        assertEquals("lots", entry.amountText)
        assertTrue(entry.isAmountError)
    }

    @Test
    fun `onEntryAmountFocusLost clears a blank amount without an error`() {
        val viewModel = viewModel()
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key
        viewModel.onEntryAmountChange(key, "  ")

        viewModel.onEntryAmountFocusLost(key)

        val entry = viewModel.state.value.entries.loadedOrNull!!.single()
        assertEquals("", entry.amountText)
        assertFalse(entry.isAmountError)
    }

    @Test
    fun `onEntryAmountChange keeps an amount error while the text is still invalid`() {
        val viewModel = viewModel()
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key
        viewModel.onEntryAmountChange(key, "lots")
        viewModel.onEntryAmountFocusLost(key)

        viewModel.onEntryAmountChange(key, "lots of")

        assertTrue(viewModel.state.value.entries.loadedOrNull!!.single().isAmountError)
    }

    @Test
    fun `onEntryAmountChange clears an amount error once the text is valid`() {
        val viewModel = viewModel()
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key
        viewModel.onEntryAmountChange(key, "lots")
        viewModel.onEntryAmountFocusLost(key)

        viewModel.onEntryAmountChange(key, "12")

        assertFalse(viewModel.state.value.entries.loadedOrNull!!.single().isAmountError)
    }

    @Test
    fun `onEntryAmountChange doesn't show an error while typing`() {
        val viewModel = viewModel()
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key

        viewModel.onEntryAmountChange(key, "lots")

        assertFalse(viewModel.state.value.entries.loadedOrNull!!.single().isAmountError)
    }

    @Test
    fun `onEntryMemoChange sets the entry's memo`() {
        val viewModel = viewModel()
        val key = viewModel.state.value.entries.loadedOrNull!!.single().key

        viewModel.onEntryMemoChange(key, "Weekly shop")

        assertEquals("Weekly shop", viewModel.state.value.entries.loadedOrNull!!.single().memo)
    }

    @Test
    fun `entry changes only change the entry with that key`() = runTest {
        val lunch = groceries.copy(categoryId = food.id, memo = "Lunch", orderInTransaction = 1)
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))
        fixture.connect(
            repository(
                categoryEntries = flowOf(listOf(groceries, lunch)),
                accounts = flowOf(listOf(bills)),
                categories = flowOf(listOf(food)),
            )
        )
        val (first, second) = viewModel.state.value.entries.loadedOrNull!!

        viewModel.onEntryCategoryChange(second.key, "Bills")
        viewModel.onEntryAmountChange(second.key, "5")
        viewModel.onEntryMemoChange(second.key, "Dinner")

        assertEquals(
            listOf(
                first,
                second.copy(
                    categoryText = "Bills",
                    category = TransactionCategory.Transfer(bills),
                    amountText = "5",
                    memo = "Dinner",
                ),
            ),
            viewModel.state.value.entries.loadedOrNull,
        )
    }

    @Test
    fun `onAddEntryClick adds an empty entry after the last one`() {
        val viewModel = viewModel()
        val first = viewModel.state.value.entries.loadedOrNull!!.single()
        viewModel.onEntryMemoChange(first.key, "First")

        viewModel.onAddEntryClick()

        val entries = viewModel.state.value.entries.loadedOrNull!!
        assertEquals(
            listOf(first.copy(memo = "First"), TransactionEntryInput(key = entries[1].key)),
            entries,
        )
        assertNotEquals(entries[0].key, entries[1].key)
    }

    @Test
    fun `onAddEntryClick does nothing while an existing transaction's entries are loading`() {
        val viewModel = viewModel(Route.TransactionDetails(existingTransaction.id))

        viewModel.onAddEntryClick()

        assertEquals(LoadState.Loading, viewModel.state.value.entries)
    }

    @Test
    fun `onRemoveEntryClick removes the entry with that key`() {
        val viewModel = viewModel()
        viewModel.onAddEntryClick()
        viewModel.onAddEntryClick()
        val (first, second, third) = viewModel.state.value.entries.loadedOrNull!!

        viewModel.onRemoveEntryClick(second.key)

        assertEquals(listOf(first, third), viewModel.state.value.entries.loadedOrNull)
    }

    @Test
    fun `onRemoveEntryClick on the only entry leaves an empty entry in its place`() {
        val viewModel = viewModel()
        val only = viewModel.state.value.entries.loadedOrNull!!.single()
        viewModel.onEntryMemoChange(only.key, "Removed")

        viewModel.onRemoveEntryClick(only.key)

        val entry = viewModel.state.value.entries.loadedOrNull!!.single()
        assertEquals(TransactionEntryInput(key = entry.key), entry)
        assertNotEquals(only.key, entry.key)
    }

    @Test
    fun `onSplitClick opens the split dialog over the form`() {
        val route = Route.NewTransaction(bills.id)
        navigator.navigate { clear(); add(Route.AccountRegister(bills.id)); add(route) }
        val viewModel = viewModel(route)

        viewModel.onSplitClick()

        assertEquals(
            listOf(Route.AccountRegister(bills.id), route, Route.TransactionSplit(route)),
            navigator.backStack.value,
        )
    }

    @Test
    fun `onSplitClick doesn't open the split dialog again while it's open`() {
        val route = Route.NewTransaction()
        navigator.navigate { clear(); add(route) }
        val viewModel = viewModel(route)

        viewModel.onSplitClick()
        viewModel.onSplitClick()

        assertEquals(listOf(route, Route.TransactionSplit(route)), navigator.backStack.value)
    }

    @Test
    fun `onSplitClick does nothing while an existing transaction's entries are loading`() {
        val route = Route.TransactionDetails(existingTransaction.id)
        navigator.navigate { clear(); add(route) }
        val viewModel = viewModel(route)

        viewModel.onSplitClick()

        assertEquals(listOf(route), navigator.backStack.value)
    }

    @Test
    fun `onSplitDoneClick closes the split dialog and keeps the entries as edited`() {
        val route = Route.NewTransaction()
        navigator.navigate { clear(); add(route) }
        val viewModel = viewModel(route)
        val first = viewModel.state.value.entries.loadedOrNull!!.single()
        viewModel.onSplitClick()
        viewModel.onEntryMemoChange(first.key, "Edited")
        viewModel.onAddEntryClick()
        val edited = viewModel.state.value.entries

        viewModel.onSplitDoneClick()

        assertEquals(listOf(route), navigator.backStack.value)
        assertEquals(edited, viewModel.state.value.entries)
    }

    @Test
    fun `onSplitCancelClick closes the split dialog and puts the entries back as they were`() {
        val route = Route.NewTransaction()
        navigator.navigate { clear(); add(route) }
        val viewModel = viewModel(route)
        val first = viewModel.state.value.entries.loadedOrNull!!.single()
        viewModel.onEntryMemoChange(first.key, "Before")
        val before = viewModel.state.value.entries
        viewModel.onSplitClick()
        viewModel.onEntryMemoChange(first.key, "Edited")
        viewModel.onAddEntryClick()

        viewModel.onSplitCancelClick()

        assertEquals(listOf(route), navigator.backStack.value)
        assertEquals(before, viewModel.state.value.entries)
    }

    @Test
    fun `onSplitCancelClick after onSplitDoneClick doesn't undo the split`() {
        val route = Route.NewTransaction()
        navigator.navigate { clear(); add(route) }
        val viewModel = viewModel(route)
        viewModel.onSplitClick()
        viewModel.onAddEntryClick()
        viewModel.onSplitDoneClick()
        val edited = viewModel.state.value.entries

        // Such as a second close request for a dialog that's already closing.
        viewModel.onSplitCancelClick()

        assertEquals(listOf(route), navigator.backStack.value)
        assertEquals(edited, viewModel.state.value.entries)
    }
}
