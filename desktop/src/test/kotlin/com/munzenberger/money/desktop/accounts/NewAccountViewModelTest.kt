package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeGroup
import com.munzenberger.money.data.api.account.AccountTypeGroupConstant
import com.munzenberger.money.data.api.account.AccountTypeGroupId
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.desktop.MoneyRepositoryControllerFixture
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NewAccountViewModelTest {

    private val navigator = Navigator()
    private val fixture = MoneyRepositoryControllerFixture()

    private val assets = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets)
    private val savings = AccountType(id = AccountTypeId(1), group = assets, value = AccountTypeConstant.Savings)
    private val checking = AccountType(id = AccountTypeId(2), group = assets, value = AccountTypeConstant.Checking)

    private fun repositoryWithAccountTypes(accountTypes: List<AccountType>) =
        mockk<MoneyRepository>(relaxUnitFun = true) {
            every { this@mockk.accountTypes } returns flowOf(accountTypes)
        }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `account type is initially empty`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        assertNull(viewModel.state.value.accountType)
    }

    @Test
    fun `account types are loaded from the connected repository in constant order`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        fixture.connect(repositoryWithAccountTypes(listOf(checking, savings)))

        assertEquals(listOf(savings, checking), viewModel.state.value.accountTypes)
    }

    @Test
    fun `onAccountTypeChange selects the account type`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        viewModel.onAccountTypeChange(checking)

        assertEquals(checking, viewModel.state.value.accountType)
    }

    @Test
    fun `onNameChange updates the name`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        viewModel.onNameChange("Checking")

        assertEquals("Checking", viewModel.state.value.name)
    }

    @Test
    fun `onBackClick pops the new account route from the back stack`() {
        navigator.navigate { clear(); add(Route.AccountList); add(Route.NewAccount) }
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        viewModel.onBackClick()

        assertEquals(listOf(Route.AccountList), navigator.backStack.toList())
        assertEquals(Route.AccountList, navigator.currentRoute.value)
    }
}
