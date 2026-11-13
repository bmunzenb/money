package com.munzenberger.money.presentation.transactions

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
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import io.mockk.every
import io.mockk.mockk
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailsViewModelTest {

    private val navigator = Navigator()
    private val fixture = MoneyRepositoryControllerFixture()

    // The real use case, a thin read over the repository mock, so these tests cover how the form treats a missing or
    // unreadable transaction without restating the use case's mapping in a mock.
    private val getTransaction = GetTransactionUseCase()

    private val today = LocalDate(2026, 5, 15)

    private fun viewModel(route: Route.TransactionFormRoute = Route.NewTransaction()) =
        TransactionDetailsViewModel(route, fixture.controller, navigator, getTransaction, today = { today })

    private val unreconciled = TransactionStatus(TransactionStatusId(1), TransactionStatusConstant.Unreconciled)

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Asset),
        value = AccountTypeConstant.Checking,
    )

    private val everyday = Account(name = "everyday", accountType = checking)
    private val bills = Account(name = "Bills", accountType = checking)

    private val existingTransaction =
        Transaction(accountId = bills.id, status = unreconciled, date = LocalDate(2026, 1, 20))

    private val groceries = CategoryEntry(
        transactionId = existingTransaction.id,
        categoryId = CategoryId(),
        amount = Money(-40),
        orderInTransaction = 0,
    )

    private fun repository(
        transaction: Flow<Transaction?> = flowOf(existingTransaction),
        categoryEntries: Flow<List<CategoryEntry>> = flowOf(listOf(groceries)),
        accounts: Flow<List<Account>> = flowOf(emptyList()),
    ) = mockk<MoneyRepository>(relaxUnitFun = true) {
        every { this@mockk.accounts } returns accounts
        every { transactionById(existingTransaction.id) } returns transaction
        every { categoryEntriesByTransactionId(existingTransaction.id) } returns categoryEntries
        every { transferEntriesByTransactionId(existingTransaction.id) } returns flowOf(emptyList())
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
    fun `onBackClick pops the transaction's route from the back stack`() {
        val route = Route.TransactionDetails(existingTransaction.id)
        navigator.navigate { clear(); add(Route.AccountRegister(existingTransaction.accountId)); add(route) }
        val viewModel = viewModel(route)

        viewModel.onBackClick()

        assertEquals(listOf(Route.AccountRegister(existingTransaction.accountId)), navigator.backStack.value)
    }
}
