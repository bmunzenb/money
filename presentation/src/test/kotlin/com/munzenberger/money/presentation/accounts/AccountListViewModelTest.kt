package com.munzenberger.money.presentation.accounts

import app.cash.turbine.test
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.preferences.InMemoryPreferences
import com.munzenberger.money.presentation.preferences.UserPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountListViewModelTest {

    private val fixture = MoneyRepositoryControllerFixture()
    private val navigator = Navigator()
    private val repository = mockk<MoneyRepository>()
    private val getAccountGroups = mockk<GetAccountGroupsUseCase>()
    private val preferences = UserPreferences(InMemoryPreferences())

    private val accountType = AccountType(
        id = AccountTypeId(1),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private fun account(name: String) = Account(name = name, accountType = accountType)

    private suspend fun viewModel(): AccountListViewModel {
        fixture.connect(repository)
        return AccountListViewModel(fixture.controller, getAccountGroups, navigator, preferences)
    }

    @Test
    fun `state stays loading while no repository is open`() = runTest {
        AccountListViewModel(fixture.controller, getAccountGroups, navigator, preferences).state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            expectNoEvents()
        }

        verify(exactly = 0) { getAccountGroups(any(), any()) }
    }

    @Test
    fun `state starts loading the account groups with no grouping`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()

        viewModel().state.test {
            assertEquals(AccountListUiState(grouping = AccountGrouping.None, groups = LoadState.Loading), awaitItem())
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
                AccountListUiState(grouping = AccountGrouping.Bank, groups = LoadState.Loaded(emptyList())),
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
    fun `state emits the account groups`() = runTest {
        val groups = listOf(AccountGroup.All(listOf(account("Checking"), account("Savings"))))
        every { getAccountGroups(any(), any()) } returns
            flowOf(Result.success(groups))

        viewModel().state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            assertEquals(AccountListUiState(groups = LoadState.Loaded(groups)), awaitItem())
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
            assertEquals(AccountListUiState(groups = LoadState.Loaded(first)), awaitItem())

            groupsFlow.value = Result.success(second)
            assertEquals(AccountListUiState(groups = LoadState.Loaded(second)), awaitItem())
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
            assertEquals(AccountListUiState(groups = LoadState.Loaded(all)), awaitItem())

            // The new grouping may show over the old groups until they're regrouped.
            viewModel.onGroupingChange(AccountGrouping.AccountType)
            assertEquals(
                AccountListUiState(grouping = AccountGrouping.AccountType, groups = LoadState.Loaded(byType)),
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
    fun `onAddAccountClick pushes the new account route`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()
        navigator.navigate { clear(); add(Route.AccountList) }

        viewModel().onAddAccountClick()

        assertEquals(listOf(Route.AccountList, Route.NewAccount), navigator.backStack.value)
    }

    @Test
    fun `onAccountClick pushes the transaction list route for the account`() = runTest {
        every { getAccountGroups(any(), any()) } returns emptyFlow()
        navigator.navigate { clear(); add(Route.AccountList) }
        val account = account("Checking")

        viewModel().onAccountClick(account)

        assertEquals(listOf(Route.AccountList, Route.TransactionList(account.id)), navigator.backStack.value)
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
