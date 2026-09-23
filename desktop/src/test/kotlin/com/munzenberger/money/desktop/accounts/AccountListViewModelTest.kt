package com.munzenberger.money.desktop.accounts

import app.cash.turbine.test
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeGroup
import com.munzenberger.money.data.api.account.AccountTypeGroupConstant
import com.munzenberger.money.data.api.account.AccountTypeGroupId
import com.munzenberger.money.data.api.account.AccountTypeId
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
        mockk<MoneyRepository> {
            every { this@mockk.accounts } returns accounts
        }

    @Test
    fun `state emits nothing when there is no repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = AccountListViewModel(controller)

        viewModel.state.test {
            expectNoEvents()
        }
    }

    @Test
    fun `state emits accounts from the connected repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = AccountListViewModel(controller)
        val accounts = listOf(account("Checking"), account("Savings"))

        viewModel.state.test {
            controller.update(repositoryWithAccounts(flowOf(accounts)))

            assertEquals(AccountListUiState(accounts = accounts), awaitItem())
        }
    }

    @Test
    fun `state reflects subsequent emissions from the repository accounts flow`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = AccountListViewModel(controller)
        val checking = account("Checking")
        val savings = account("Savings")
        val accountsFlow = MutableStateFlow(listOf(checking))

        viewModel.state.test {
            controller.update(repositoryWithAccounts(accountsFlow))
            assertEquals(AccountListUiState(accounts = listOf(checking)), awaitItem())

            accountsFlow.value = listOf(checking, savings)
            assertEquals(AccountListUiState(accounts = listOf(checking, savings)), awaitItem())
        }
    }

    @Test
    fun `state switches to the newly connected repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = AccountListViewModel(controller)
        val firstAccounts = listOf(account("Checking"))
        val secondAccounts = listOf(account("Savings"))

        viewModel.state.test {
            controller.update(repositoryWithAccounts(flowOf(firstAccounts)))
            assertEquals(AccountListUiState(accounts = firstAccounts), awaitItem())

            controller.update(repositoryWithAccounts(flowOf(secondAccounts)))
            assertEquals(AccountListUiState(accounts = secondAccounts), awaitItem())
        }
    }
}
