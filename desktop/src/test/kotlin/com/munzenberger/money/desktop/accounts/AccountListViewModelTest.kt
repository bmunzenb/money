package com.munzenberger.money.desktop.accounts

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeGroup
import com.munzenberger.money.data.api.account.AccountTypeGroupConstant
import com.munzenberger.money.data.api.account.AccountTypeGroupId
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.desktop.MoneyRepositoryControllerFixture
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountListViewModelTest {

    private val accountType = AccountType(
        id = AccountTypeId(1),
        group = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets),
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
        val viewModel = AccountListViewModel(fixture.controller)

        viewModel.state.test {
            expectNoEvents()
        }
    }

    @Test
    fun `state emits accounts from the connected repository`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = AccountListViewModel(fixture.controller)
        val accounts = listOf(account("Checking"), account("Savings"))

        viewModel.state.test {
            fixture.connect(repositoryWithAccounts(flowOf(accounts)))

            assertEquals(AccountListUiState.Content(accounts = accounts), awaitItem())
        }
    }

    @Test
    fun `state reflects subsequent emissions from the repository accounts flow`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = AccountListViewModel(fixture.controller)
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
        val viewModel = AccountListViewModel(fixture.controller)
        val firstAccounts = listOf(account("Checking"))
        val secondAccounts = listOf(account("Savings"))

        viewModel.state.test {
            fixture.connect(repositoryWithAccounts(flowOf(firstAccounts)))
            assertEquals(AccountListUiState.Content(accounts = firstAccounts), awaitItem())

            fixture.connect(repositoryWithAccounts(flowOf(secondAccounts)))
            assertEquals(AccountListUiState.Content(accounts = secondAccounts), awaitItem())
        }
    }
}
