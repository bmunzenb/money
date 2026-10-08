package com.munzenberger.money.desktop.accounts

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.desktop.MoneyRepositoryControllerFixture
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountListViewModelTest {

    private val navigator = Navigator()

    private val accountType = AccountType(
        id = AccountTypeId(1),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private fun account(name: String) = Account(name = name, accountType = accountType)

    private fun repositoryWithAccounts(accounts: Flow<List<Account>>) =
        mockk<MoneyRepository>(relaxUnitFun = true) {
            every { this@mockk.accounts } returns accounts
        }

    @Test
    fun `state emits nothing when there is no repository`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = AccountListViewModel(fixture.controller, navigator)

        viewModel.state.test {
            expectNoEvents()
        }
    }

    @Test
    fun `state emits accounts from the connected repository`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = AccountListViewModel(fixture.controller, navigator)
        val accounts = listOf(account("Checking"), account("Savings"))

        viewModel.state.test {
            fixture.connect(repositoryWithAccounts(flowOf(accounts)))

            assertEquals(AccountListUiState.Content(accounts = accounts), awaitItem())
        }
    }

    @Test
    fun `state reflects subsequent emissions from the repository accounts flow`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = AccountListViewModel(fixture.controller, navigator)
        val checking = account("Checking")
        val savings = account("Savings")
        val accountsFlow = MutableStateFlow(listOf(checking))

        viewModel.state.test {
            fixture.connect(repositoryWithAccounts(accountsFlow))
            assertEquals(AccountListUiState.Content(accounts = listOf(checking)), awaitItem())

            accountsFlow.value = listOf(checking, savings)
            assertEquals(AccountListUiState.Content(accounts = listOf(checking, savings)), awaitItem())
        }
    }

    @Test
    fun `state switches to the newly connected repository`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = AccountListViewModel(fixture.controller, navigator)
        val firstAccounts = listOf(account("Checking"))
        val secondAccounts = listOf(account("Savings"))

        viewModel.state.test {
            fixture.connect(repositoryWithAccounts(flowOf(firstAccounts)))
            assertEquals(AccountListUiState.Content(accounts = firstAccounts), awaitItem())

            fixture.connect(repositoryWithAccounts(flowOf(secondAccounts)))
            assertEquals(AccountListUiState.Content(accounts = secondAccounts), awaitItem())
        }
    }

    @Test
    fun `state emits error when the repository accounts flow throws`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = AccountListViewModel(fixture.controller, navigator)

        viewModel.state.test {
            fixture.connect(repositoryWithAccounts(flow { throw IllegalStateException("query failed") }))

            assertEquals(AccountListUiState.Error, awaitItem())
        }
    }

    @Test
    fun `state recovers from an error when a new repository is connected`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = AccountListViewModel(fixture.controller, navigator)
        val accounts = listOf(account("Checking"))

        viewModel.state.test {
            fixture.connect(repositoryWithAccounts(flow { throw IllegalStateException("query failed") }))
            assertEquals(AccountListUiState.Error, awaitItem())

            fixture.connect(repositoryWithAccounts(flowOf(accounts)))
            assertEquals(AccountListUiState.Content(accounts = accounts), awaitItem())
        }
    }

    @Test
    fun `onAddAccountClick pushes the new account route`() {
        navigator.navigate { clear(); add(Route.AccountList) }
        val viewModel = AccountListViewModel(MoneyRepositoryControllerFixture().controller, navigator)

        viewModel.onAddAccountClick()

        assertEquals(listOf(Route.AccountList, Route.NewAccount), navigator.backStack.toList())
    }
}
