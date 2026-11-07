package com.munzenberger.money.presentation.accounts

import app.cash.turbine.test
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.core.account.GetAccountBalancesUseCase
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
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
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.preferences.InMemoryPreferences
import com.munzenberger.money.presentation.preferences.UserPreferences
import com.munzenberger.money.presentation.state.LoadState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AccountListViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val fixture = MoneyRepositoryControllerFixture()
    private val navigator = Navigator()
    private val repository = mockk<MoneyRepository>()
    private val getAccountGroups = mockk<GetAccountGroupsUseCase>()
    private val getAccountBalances = mockk<GetAccountBalancesUseCase> {
        every { this@mockk(any()) } returns emptyFlow()
    }
    private val preferencesNode = InMemoryPreferences()
    private val preferences = UserPreferences(preferencesNode)

    private val accountType = AccountType(
        id = AccountTypeId(1),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Asset),
        value = AccountTypeConstant.Checking,
    )

    private fun account(name: String) = Account(name = name, accountType = accountType)

    // The state with the loaded groups, before their balances load.
    private fun loaded(groups: List<AccountGroup>, grouping: AccountGrouping = AccountGrouping.None) =
        AccountListUiState(grouping, LoadState.Loaded(groups), accountBalances(groups, null))

    private suspend fun viewModel(connect: Boolean = true): AccountListViewModel {
        if (connect) fixture.connect(repository)
        return AccountListViewModel(fixture.controller, getAccountGroups, getAccountBalances, navigator, preferences)
    }

    @Test
    fun `state stays loading while no repository is open`() = runTest {
        viewModel(connect = false).state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            runCurrent()
            expectNoEvents()
        }

        verify(exactly = 0) { getAccountGroups(any(), any()) }
    }

    @Test
    fun `state starts loading the account groups with no grouping`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()

        viewModel().state.test {
            assertEquals(AccountListUiState(grouping = AccountGrouping.None, groups = LoadState.Loading), awaitItem())
            runCurrent()
        }

        verify(exactly = 1) { getAccountGroups(repository, any()) }
    }

    @Test
    fun `state starts with the saved grouping`() = runTest {
        preferences.accountGrouping = AccountGrouping.Bank
        every { getAccountGroups(any(), any()) } answers {
            secondArg<Flow<AccountGrouping>>().map { grouping ->
                assertEquals(AccountGrouping.Bank, grouping)
                Result.success(emptyList())
            }
        }

        viewModel().state.test {
            assertEquals(AccountListUiState(grouping = AccountGrouping.Bank, groups = LoadState.Loading), awaitItem())
            assertEquals(
                loaded(emptyList(), AccountGrouping.Bank),
                awaitItem(),
            )
        }
    }

    @Test
    fun `onGroupingChange saves the grouping`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()

        viewModel().onGroupingChange(AccountGrouping.AccountClass)

        assertEquals(AccountGrouping.AccountClass, preferences.accountGrouping)
    }

    @Test
    fun `onGroupingChange doesn't save the current grouping`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()

        viewModel().onGroupingChange(AccountGrouping.None)

        assertEquals(emptyList(), preferencesNode.keys().toList())
    }

    @Test
    fun `state emits the account groups`() = runTest {
        val groups = listOf(AccountGroup.All(listOf(account("Checking"), account("Savings"))))
        every { getAccountGroups(any(), any()) } returns
            flowOf(Result.success(groups))

        viewModel().state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            assertEquals(loaded(groups), awaitItem())
        }
    }

    @Test
    fun `state reflects subsequent emissions of the account groups`() = runTest {
        val first = listOf(AccountGroup.All(listOf(account("Checking"))))
        val second = listOf(AccountGroup.All(listOf(account("Checking"), account("Savings"))))
        val groupsFlow = MutableStateFlow(Result.success<List<AccountGroup>>(first))
        every { getAccountGroups(any(), any()) } returns groupsFlow

        viewModel().state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            assertEquals(loaded(first), awaitItem())

            groupsFlow.value = Result.success(second)
            assertEquals(loaded(second), awaitItem())
        }
    }

    @Test
    fun `state emits error when getting the account groups fails`() = runTest {
        every { getAccountGroups(any(), any()) } returns
            flowOf(Result.failure(IllegalStateException("query failed")))

        viewModel().state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            assertEquals(AccountListUiState(groups = LoadState.Error), awaitItem())
        }
    }

    @Test
    fun `state emits the balances of the account groups once they load`() = runTest {
        val checking = account("Checking")
        val savings = account("Savings")
        val groups = listOf(AccountGroup.ByAccountType(accountType, listOf(checking, savings)))
        val balances = MutableSharedFlow<Result<Map<AccountId, Money>>>()
        every { getAccountGroups(any(), any()) } returns flowOf(Result.success(groups))
        every { getAccountBalances(any()) } returns balances

        viewModel().state.test {
            runCurrent()
            assertEquals(loaded(groups), expectMostRecentItem())

            balances.emit(Result.success(mapOf(checking.id to Money(100), savings.id to Money(200))))
            runCurrent()
            assertEquals(
                AccountListUiState(
                    groups = LoadState.Loaded(groups),
                    balances = AccountBalances(
                        accounts = mapOf(
                            checking.id to LoadState.Loaded(Money(100)),
                            savings.id to LoadState.Loaded(Money(200)),
                        ),
                        subtotals = mapOf(groups.single() to LoadState.Loaded(Money(300))),
                        total = LoadState.Loaded(Money(300)),
                    ),
                ),
                expectMostRecentItem(),
            )
        }

        verify(exactly = 1) { getAccountBalances(repository) }
    }

    @Test
    fun `onGroupingChange switches to the groups for the new grouping`() = runTest {
        val all = listOf(AccountGroup.All(listOf(account("Checking"))))
        val byType = listOf(AccountGroup.ByAccountType(accountType, listOf(account("Checking"))))
        every { getAccountGroups(any(), any()) } answers {
            secondArg<Flow<AccountGrouping>>().map { grouping ->
                Result.success(if (grouping == AccountGrouping.AccountType) byType else all)
            }
        }
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            assertEquals(loaded(all), awaitItem())

            // The new grouping may show over the old groups until they're regrouped.
            viewModel.onGroupingChange(AccountGrouping.AccountType)
            runCurrent()
            assertEquals(
                loaded(byType, AccountGrouping.AccountType),
                expectMostRecentItem(),
            )
        }

        verify(exactly = 1) { getAccountGroups(repository, any()) }
    }

    @Test
    fun `onGroupingChange shows the selected grouping while loading`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())

            viewModel.onGroupingChange(AccountGrouping.Bank)
            assertEquals(AccountListUiState(grouping = AccountGrouping.Bank, groups = LoadState.Loading), awaitItem())
        }
    }

    @Test
    fun `onGroupingChange shows the selected grouping after an error`() = runTest {
        every { getAccountGroups(any(), any()) } returns
            flowOf(Result.failure(IllegalStateException("query failed")))
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            assertEquals(AccountListUiState(groups = LoadState.Error), awaitItem())

            viewModel.onGroupingChange(AccountGrouping.Bank)
            assertEquals(AccountListUiState(grouping = AccountGrouping.Bank, groups = LoadState.Error), awaitItem())
        }
    }

    @Test
    fun `onNewAccountClick pushes the new account route`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()
        navigator.navigate { clear(); add(Route.AccountList) }

        viewModel().onNewAccountClick()

        assertEquals(listOf(Route.AccountList, Route.NewAccount), navigator.backStack.value)
    }

    @Test
    fun `onAccountClick pushes the transaction list route for the account`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()
        navigator.navigate { clear(); add(Route.AccountList) }
        val account = account("Checking")

        viewModel().onAccountClick(account)

        assertEquals(listOf(Route.AccountList, Route.AccountRegister(account.id)), navigator.backStack.value)
    }

    @Test
    fun `onAccountSettingsClick pushes the account settings route for the account`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()
        navigator.navigate { clear(); add(Route.AccountList) }
        val account = account("Checking")

        viewModel().onAccountSettingsClick(account)

        assertEquals(listOf(Route.AccountList, Route.AccountSettings(account.id)), navigator.backStack.value)
    }
}
