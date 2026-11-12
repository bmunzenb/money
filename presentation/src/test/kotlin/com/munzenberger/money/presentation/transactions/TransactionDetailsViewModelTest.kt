package com.munzenberger.money.presentation.transactions

import com.munzenberger.money.core.transaction.GetTransactionUseCase
import com.munzenberger.money.core.transaction.TransactionWithEntries
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

    private fun viewModel(route: Route.TransactionFormRoute = Route.NewTransaction()) =
        TransactionDetailsViewModel(route, fixture.controller, navigator, getTransaction)

    private val unreconciled = TransactionStatus(TransactionStatusId(1), TransactionStatusConstant.Unreconciled)

    private val existingTransaction = Transaction(accountId = AccountId(), status = unreconciled)

    private val groceries = CategoryEntry(
        transactionId = existingTransaction.id,
        categoryId = CategoryId(),
        amount = Money(-40),
        orderInTransaction = 0,
    )

    private fun repository(
        transaction: Flow<Transaction?> = flowOf(existingTransaction),
        categoryEntries: Flow<List<CategoryEntry>> = flowOf(listOf(groceries)),
    ) = mockk<MoneyRepository>(relaxUnitFun = true) {
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
    fun `onBackClick pops the transaction's route from the back stack`() {
        val route = Route.TransactionDetails(existingTransaction.id)
        navigator.navigate { clear(); add(Route.AccountRegister(existingTransaction.accountId)); add(route) }
        val viewModel = viewModel(route)

        viewModel.onBackClick()

        assertEquals(listOf(Route.AccountRegister(existingTransaction.accountId)), navigator.backStack.value)
    }
}
