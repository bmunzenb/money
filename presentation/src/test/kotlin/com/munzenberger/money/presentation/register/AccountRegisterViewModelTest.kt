package com.munzenberger.money.presentation.register

import app.cash.turbine.test
import com.munzenberger.money.core.account.GetAccountBalanceUseCase
import com.munzenberger.money.core.account.GetAccountUseCase
import com.munzenberger.money.core.register.GetAccountRegisterUseCase
import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.transaction.CategoryEntryId
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntryId
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountRegisterViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val fixture = MoneyRepositoryControllerFixture()
    private val repository = mockk<MoneyRepository>(relaxUnitFun = true)
    private val getAccount = mockk<GetAccountUseCase>()

    // Emits no balance unless a test stubs one, so the balance stays loading.
    private val getAccountBalance = mockk<GetAccountBalanceUseCase> {
        every { this@mockk(any(), any()) } returns emptyFlow()
    }

    // Emits no entries unless a test stubs them, so the entries stay loading.
    private val getAccountRegister = mockk<GetAccountRegisterUseCase> {
        every { this@mockk(any(), any()) } returns emptyFlow()
    }
    private val navigator = Navigator()

    private val account = Account(
        name = "Checking",
        accountType = AccountType(
            id = AccountTypeId(2),
            accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
            value = AccountTypeConstant.Checking,
        ),
    )

    private val creditCard = Account(
        name = "Credit Card",
        accountType = AccountType(
            id = AccountTypeId(5),
            accountClass = AccountClass(id = AccountClassId(2), value = AccountClassConstant.Liabilities),
            value = AccountTypeConstant.Credit,
        ),
    )

    private val entry = RegisterEntry.Transaction(
        transactionId = TransactionId(),
        payeeId = null,
        payeeName = "Grocer",
        date = LocalDate(2026, 1, 15),
        amount = Money(-2500),
        balance = Money(7500),
        number = null,
        memo = null,
        status = TransactionStatus(TransactionStatusId(1), TransactionStatusConstant.Unreconciled),
        entries = emptyList(),
    )

    private suspend fun viewModel(connect: Boolean = true): AccountRegisterViewModel {
        if (connect) fixture.connect(repository)
        return AccountRegisterViewModel(
            Route.AccountRegister(account.id),
            fixture.controller,
            getAccount,
            getAccountBalance,
            getAccountRegister,
            navigator,
        )
    }

    @Test
    fun `state stays loading while no repository is open`() = runTest {
        viewModel(connect = false).state.test {
            assertEquals(AccountRegisterUiState(account = LoadState.Loading), awaitItem())
            runCurrent()
            expectNoEvents()
        }

        verify(exactly = 0) { getAccount(any(), any()) }
        verify(exactly = 0) { getAccountBalance(any(), any()) }
        verify(exactly = 0) { getAccountRegister(any(), any()) }
    }

    @Test
    fun `state loads the route's account from the open repository`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))

        viewModel().state.test {
            assertEquals(AccountRegisterUiState(account = LoadState.Loading), awaitItem())
            assertEquals(AccountRegisterUiState(account = LoadState.Loaded(account)), awaitItem())
        }

        verify(exactly = 1) { getAccount(repository, account.id) }
    }

    @Test
    fun `state follows changes to the account`() = runTest {
        val results = MutableStateFlow(Result.success(account))
        every { getAccount(any(), any()) } returns results

        viewModel().state.test {
            skipItems(2)

            val renamed = account.copy(name = "Everyday")
            results.value = Result.success(renamed)

            assertEquals(AccountRegisterUiState(account = LoadState.Loaded(renamed)), awaitItem())
        }
    }

    @Test
    fun `state is an error when there's no such account`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.failure(EntityNotFoundException("No account")))

        viewModel().state.test {
            skipItems(1)
            assertEquals(
                AccountRegisterUiState(account = LoadState.Error, balance = LoadState.Error, entries = LoadState.Error),
                awaitItem(),
            )
        }
    }

    @Test
    fun `state is an error when the account is removed`() = runTest {
        val results = MutableStateFlow(Result.success(account))
        every { getAccount(any(), any()) } returns results

        viewModel().state.test {
            skipItems(2)

            results.value = Result.failure(EntityNotFoundException("No account"))

            assertEquals(
                AccountRegisterUiState(account = LoadState.Error, balance = LoadState.Error, entries = LoadState.Error),
                awaitItem(),
            )
        }
    }

    @Test
    fun `state is an error when the account can't be loaded`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.failure(IllegalStateException("query failed")))

        viewModel().state.test {
            skipItems(1)
            assertEquals(
                AccountRegisterUiState(account = LoadState.Error, balance = LoadState.Error, entries = LoadState.Error),
                awaitItem(),
            )
        }
    }

    @Test
    fun `state loads the route's account's balance from the open repository`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        every { getAccountBalance(any(), any()) } returns flowOf(Result.success(Money(1000)))

        viewModel().state.test {
            runCurrent()
            assertEquals(
                AccountRegisterUiState(account = LoadState.Loaded(account), balance = LoadState.Loaded(Money(1000))),
                expectMostRecentItem(),
            )
        }

        verify(exactly = 1) { getAccountBalance(repository, account.id) }
    }

    @Test
    fun `state shows the account while its balance loads`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))

        viewModel().state.test {
            skipItems(1)
            assertEquals(
                AccountRegisterUiState(account = LoadState.Loaded(account), balance = LoadState.Loading),
                awaitItem(),
            )
        }
    }

    @Test
    fun `state's balance and entries stay loading while the account loads`() = runTest {
        every { getAccount(any(), any()) } returns emptyFlow()
        every { getAccountBalance(any(), any()) } returns flowOf(Result.success(Money(1000)))
        every { getAccountRegister(any(), any()) } returns flowOf(Result.success(listOf(entry)))

        viewModel().state.test {
            runCurrent()
            assertEquals(AccountRegisterUiState(), expectMostRecentItem())
        }
    }

    @Test
    fun `state negates a liability account's balance and entries`() = runTest {
        val categoryEntry = RegisterEntry.Transaction.Entry.Category(
            categoryEntryId = CategoryEntryId(),
            categoryId = CategoryId(),
            categoryName = "Dining",
            parentCategoryId = null,
            parentCategoryName = null,
            amount = Money(-2500),
        )
        val purchase = entry.copy(entries = listOf(categoryEntry))
        val transfer = RegisterEntry.Transfer(
            transferEntryId = TransferEntryId(),
            payeeId = null,
            payeeName = null,
            date = LocalDate(2026, 1, 20),
            amount = Money(5000),
            balance = Money(2500),
            number = null,
            memo = null,
            status = entry.status,
            accountId = AccountId(),
            accountName = "Checking",
        )
        every { getAccount(any(), any()) } returns flowOf(Result.success(creditCard))
        every { getAccountBalance(any(), any()) } returns flowOf(Result.success(Money(-2500)))
        every { getAccountRegister(any(), any()) } returns flowOf(Result.success(listOf(purchase, transfer)))

        viewModel().state.test {
            runCurrent()
            val state = expectMostRecentItem()
            assertEquals(LoadState.Loaded(Money(2500)), state.balance)
            assertEquals(
                LoadState.Loaded(
                    listOf(
                        purchase.copy(
                            amount = Money(2500),
                            balance = Money(-7500),
                            entries = listOf(categoryEntry.copy(amount = Money(2500))),
                        ),
                        transfer.copy(amount = Money(-5000), balance = Money(-2500)),
                    )
                ),
                state.entries,
            )
        }
    }

    @Test
    fun `state keeps an asset account's balance and entries' signs`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        every { getAccountBalance(any(), any()) } returns flowOf(Result.success(Money(-2500)))
        every { getAccountRegister(any(), any()) } returns flowOf(Result.success(listOf(entry)))

        viewModel().state.test {
            runCurrent()
            val state = expectMostRecentItem()
            assertEquals(LoadState.Loaded(Money(-2500)), state.balance)
            assertEquals(LoadState.Loaded(listOf(entry)), state.entries)
        }
    }

    @Test
    fun `state follows changes to the balance`() = runTest {
        val balances = MutableStateFlow(Result.success(Money(1000)))
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        every { getAccountBalance(any(), any()) } returns balances

        viewModel().state.test {
            runCurrent()
            expectMostRecentItem()

            balances.value = Result.success(Money(750))

            assertEquals(LoadState.Loaded(Money(750)), awaitItem().balance)
        }
    }

    @Test
    fun `state's balance is an error when it can't be loaded`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        every { getAccountBalance(any(), any()) } returns
            flowOf(Result.failure(IllegalStateException("query failed")))

        viewModel().state.test {
            runCurrent()
            assertEquals(
                AccountRegisterUiState(account = LoadState.Loaded(account), balance = LoadState.Error),
                expectMostRecentItem(),
            )
        }
    }

    @Test
    fun `state loads the route's account's register entries from the open repository`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        every { getAccountRegister(any(), any()) } returns flowOf(Result.success(listOf(entry)))

        viewModel().state.test {
            runCurrent()
            assertEquals(LoadState.Loaded(listOf(entry)), expectMostRecentItem().entries)
        }

        verify(exactly = 1) { getAccountRegister(repository, account.id) }
    }

    @Test
    fun `state shows the account while its register entries load`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))

        viewModel().state.test {
            runCurrent()
            val state = expectMostRecentItem()
            assertEquals(LoadState.Loaded(account), state.account)
            assertEquals(LoadState.Loading, state.entries)
        }
    }

    @Test
    fun `state follows changes to the register entries`() = runTest {
        val results = MutableStateFlow(Result.success(listOf(entry)))
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        every { getAccountRegister(any(), any()) } returns results

        viewModel().state.test {
            runCurrent()
            expectMostRecentItem()

            val added = entry.copy(transactionId = TransactionId(), amount = Money(1000), balance = Money(8500))
            results.value = Result.success(listOf(entry, added))

            assertEquals(LoadState.Loaded(listOf(entry, added)), awaitItem().entries)
        }
    }

    @Test
    fun `state's register entries are an error when they can't be loaded`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        every { getAccountRegister(any(), any()) } returns
            flowOf(Result.failure(IllegalStateException("query failed")))

        viewModel().state.test {
            runCurrent()
            val state = expectMostRecentItem()
            assertEquals(LoadState.Loaded(account), state.account)
            assertEquals(LoadState.Error, state.entries)
        }
    }

    @Test
    fun `state starts loading again when another repository opens`() = runTest {
        val other = mockk<MoneyRepository>()
        every { getAccount(repository, any()) } returns flowOf(Result.success(account))
        every { getAccountBalance(repository, any()) } returns flowOf(Result.success(Money(1000)))
        every { getAccountRegister(repository, any()) } returns flowOf(Result.success(listOf(entry)))
        every { getAccount(other, any()) } returns emptyFlow()
        every { getAccountBalance(other, any()) } returns emptyFlow()
        every { getAccountRegister(other, any()) } returns emptyFlow()

        viewModel().state.test {
            runCurrent()
            expectMostRecentItem()

            fixture.connect(other)

            assertEquals(AccountRegisterUiState(), awaitItem())
        }
    }

    @Test
    fun `onAccountSettingsClick pushes the account settings route for the route's account`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        navigator.navigate { clear(); add(Route.AccountRegister(account.id)) }
        val viewModel = viewModel()

        viewModel.state.test {
            runCurrent()
            assertTrue(expectMostRecentItem().canOpenAccountSettings)

            viewModel.onAccountSettingsClick()
        }

        assertEquals(
            listOf(Route.AccountRegister(account.id), Route.AccountSettings(account.id)),
            navigator.backStack.value,
        )
    }

    @Test
    fun `onAccountSettingsClick does nothing while the account loads`() = runTest {
        every { getAccount(any(), any()) } returns emptyFlow()
        navigator.navigate { clear(); add(Route.AccountRegister(account.id)) }
        val viewModel = viewModel()

        viewModel.state.test {
            runCurrent()
            assertFalse(expectMostRecentItem().canOpenAccountSettings)

            viewModel.onAccountSettingsClick()
        }

        assertEquals(listOf(Route.AccountRegister(account.id)), navigator.backStack.value)
    }

    @Test
    fun `onAccountSettingsClick does nothing when the account can't be loaded`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.failure(EntityNotFoundException("No account")))
        navigator.navigate { clear(); add(Route.AccountRegister(account.id)) }
        val viewModel = viewModel()

        viewModel.state.test {
            runCurrent()
            assertFalse(expectMostRecentItem().canOpenAccountSettings)

            viewModel.onAccountSettingsClick()
        }

        assertEquals(listOf(Route.AccountRegister(account.id)), navigator.backStack.value)
    }

    @Test
    fun `onNewTransactionClick pushes the new transaction route for the route's account`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.success(account))
        navigator.navigate { clear(); add(Route.AccountRegister(account.id)) }
        val viewModel = viewModel()

        viewModel.state.test {
            runCurrent()
            assertTrue(expectMostRecentItem().canAddTransaction)

            viewModel.onNewTransactionClick()
        }

        assertEquals(
            listOf(Route.AccountRegister(account.id), Route.NewTransaction(account.id)),
            navigator.backStack.value,
        )
    }

    @Test
    fun `onNewTransactionClick does nothing while the account loads`() = runTest {
        every { getAccount(any(), any()) } returns emptyFlow()
        navigator.navigate { clear(); add(Route.AccountRegister(account.id)) }
        val viewModel = viewModel()

        viewModel.state.test {
            runCurrent()
            assertFalse(expectMostRecentItem().canAddTransaction)

            viewModel.onNewTransactionClick()
        }

        assertEquals(listOf(Route.AccountRegister(account.id)), navigator.backStack.value)
    }

    @Test
    fun `onNewTransactionClick does nothing when the account can't be loaded`() = runTest {
        every { getAccount(any(), any()) } returns flowOf(Result.failure(EntityNotFoundException("No account")))
        navigator.navigate { clear(); add(Route.AccountRegister(account.id)) }
        val viewModel = viewModel()

        viewModel.state.test {
            runCurrent()
            assertFalse(expectMostRecentItem().canAddTransaction)

            viewModel.onNewTransactionClick()
        }

        assertEquals(listOf(Route.AccountRegister(account.id)), navigator.backStack.value)
    }
}
