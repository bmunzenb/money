package com.munzenberger.money.desktop.accounts

import app.cash.turbine.test
import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
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

    private val navigator = Navigator()
    private val getAccountGroups = mockk<GetAccountGroupsUseCase>()

    private val accountType = AccountType(
        id = AccountTypeId(1),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private fun account(name: String) = Account(name = name, accountType = accountType)

    private fun viewModel() = AccountListViewModel(getAccountGroups, navigator)

    @Test
    fun `state starts loading the account groups with no grouping`() = runTest {
        every { getAccountGroups(any()) } returns emptyFlow()

        viewModel().state.test {
            assertEquals(AccountListUiState(grouping = AccountGrouping.None, groups = LoadState.Loading), awaitItem())
        }

        verify(exactly = 1) { getAccountGroups(any()) }
    }

    @Test
    fun `state emits the account groups`() = runTest {
        val groups = listOf(AccountGroup.All(listOf(account("Checking"), account("Savings"))))
        every { getAccountGroups(any()) } returns
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
        every { getAccountGroups(any()) } returns groupsFlow

        viewModel().state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())
            assertEquals(AccountListUiState(groups = LoadState.Loaded(first)), awaitItem())

            groupsFlow.value = Result.success(second)
            assertEquals(AccountListUiState(groups = LoadState.Loaded(second)), awaitItem())
        }
    }

    @Test
    fun `state emits error when getting the account groups fails`() = runTest {
        every { getAccountGroups(any()) } returns
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
        every { getAccountGroups(any()) } answers {
            firstArg<Flow<AccountGrouping>>().map { grouping ->
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

        verify(exactly = 1) { getAccountGroups(any()) }
    }

    @Test
    fun `onGroupingChange shows the selected grouping while loading`() = runTest {
        every { getAccountGroups(any()) } returns emptyFlow()
        val viewModel = viewModel()

        viewModel.state.test {
            assertEquals(AccountListUiState(groups = LoadState.Loading), awaitItem())

            viewModel.onGroupingChange(AccountGrouping.Bank)
            assertEquals(AccountListUiState(grouping = AccountGrouping.Bank, groups = LoadState.Loading), awaitItem())
        }
    }

    @Test
    fun `onGroupingChange shows the selected grouping after an error`() = runTest {
        every { getAccountGroups(any()) } returns
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
    fun `onAddAccountClick pushes the new account route`() {
        every { getAccountGroups(any()) } returns emptyFlow()
        navigator.navigate { clear(); add(Route.AccountList) }

        viewModel().onAddAccountClick()

        assertEquals(listOf(Route.AccountList, Route.NewAccount), navigator.backStack.toList())
    }
}
