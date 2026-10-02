package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeGroup
import com.munzenberger.money.data.api.account.AccountTypeGroupConstant
import com.munzenberger.money.data.api.account.AccountTypeGroupId
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.desktop.MoneyRepositoryControllerFixture
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NewAccountViewModelTest {

    private val navigator = Navigator()
    private val fixture = MoneyRepositoryControllerFixture()

    private val assets = AccountTypeGroup(id = AccountTypeGroupId(1), value = AccountTypeGroupConstant.Assets)
    private val savings = AccountType(id = AccountTypeId(1), group = assets, value = AccountTypeConstant.Savings)
    private val checking = AccountType(id = AccountTypeId(2), group = assets, value = AccountTypeConstant.Checking)

    private val firstBank = Bank(name = "First Bank")
    private val creditUnion = Bank(name = "credit union")

    private fun repository(
        accountTypes: Flow<List<AccountType>> = flowOf(emptyList()),
        banks: Flow<List<Bank>> = flowOf(emptyList()),
    ) = mockk<MoneyRepository>(relaxUnitFun = true) {
        every { this@mockk.accountTypes } returns accountTypes
        every { this@mockk.banks } returns banks
    }

    private fun <T> failingFlow(): Flow<T> = flow { error("query failed") }

    // The initial balance is parsed and formatted for the default locale.
    private val defaultLocale = Locale.getDefault()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Locale.setDefault(Locale.US)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `account type is initially empty`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        assertNull(viewModel.state.value.accountType)
    }

    @Test
    fun `account types are loaded from the connected repository in constant order`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        fixture.connect(repository(accountTypes = flowOf(listOf(checking, savings))))

        assertEquals(LoadState.Loaded(listOf(savings, checking)), viewModel.state.value.accountTypes)
    }

    @Test
    fun `account types and banks are loading until a repository is connected`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        assertEquals(LoadState.Loading, viewModel.state.value.accountTypes)
        assertEquals(LoadState.Loading, viewModel.state.value.banks)
    }

    @Test
    fun `account types are an error when they can't be loaded`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        fixture.connect(repository(accountTypes = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.accountTypes)
    }

    @Test
    fun `banks are an error when they can't be loaded, and a typed name is still remembered`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        fixture.connect(repository(banks = failingFlow()))

        viewModel.onBankNameChange("New Bank")

        assertEquals(LoadState.Error, viewModel.state.value.banks)
        assertEquals("New Bank", viewModel.state.value.bankName)
        assertNull(viewModel.state.value.bank)
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
    fun `banks are loaded from the connected repository sorted by name`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        fixture.connect(repository(banks = flowOf(listOf(firstBank, creditUnion))))

        assertEquals(LoadState.Loaded(listOf(creditUnion, firstBank)), viewModel.state.value.banks)
    }

    @Test
    fun `financial institution is initially blank`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        assertEquals("", viewModel.state.value.bankName)
        assertNull(viewModel.state.value.bank)
    }

    @Test
    fun `onBankChange selects the existing bank`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        fixture.connect(repository(banks = flowOf(listOf(firstBank, creditUnion))))

        viewModel.onBankChange(firstBank)

        assertEquals("First Bank", viewModel.state.value.bankName)
        assertEquals(firstBank, viewModel.state.value.bank)
    }

    @Test
    fun `onBankNameChange with a new name remembers the name without a bank`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        fixture.connect(repository(banks = flowOf(listOf(firstBank))))

        viewModel.onBankNameChange("New Bank")

        assertEquals("New Bank", viewModel.state.value.bankName)
        assertNull(viewModel.state.value.bank)
    }

    @Test
    fun `onBankNameChange with an existing bank's name selects that bank`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        fixture.connect(repository(banks = flowOf(listOf(firstBank))))

        viewModel.onBankNameChange(" first bank ")

        assertEquals(" first bank ", viewModel.state.value.bankName)
        assertEquals(firstBank, viewModel.state.value.bank)
    }

    @Test
    fun `editing a selected bank's name clears the selection`() = runTest {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        fixture.connect(repository(banks = flowOf(listOf(firstBank))))
        viewModel.onBankChange(firstBank)

        viewModel.onBankNameChange("First Bank of Detroit")

        assertNull(viewModel.state.value.bank)
    }

    @Test
    fun `account number is initially blank`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        assertEquals("", viewModel.state.value.number)
    }

    @Test
    fun `onNumberChange updates the account number`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        viewModel.onNumberChange("1234-5678")

        assertEquals("1234-5678", viewModel.state.value.number)
    }

    @Test
    fun `initial balance is initially blank without an error`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        assertEquals("", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `currency symbol is the default currency's symbol`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        assertEquals("$", viewModel.state.value.currencySymbol)
    }

    @Test
    fun `an invalid initial balance isn't an error while still typing`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        viewModel.onInitialBalanceChange("-")

        assertEquals("-", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `an invalid initial balance is an error once focus is lost`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        viewModel.onInitialBalanceChange("12abc")

        viewModel.onInitialBalanceFocusLost()

        assertEquals("12abc", viewModel.state.value.initialBalance)
        assertTrue(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `correcting an invalid initial balance clears the error`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        viewModel.onInitialBalanceChange("12abc")
        viewModel.onInitialBalanceFocusLost()

        viewModel.onInitialBalanceChange("12")

        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `a valid initial balance is reformatted once focus is lost`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        viewModel.onInitialBalanceChange("1234.5")

        viewModel.onInitialBalanceFocusLost()

        assertEquals("1,234.50", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `a negative initial balance is valid`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        viewModel.onInitialBalanceChange("-250")

        viewModel.onInitialBalanceFocusLost()

        assertEquals("-250.00", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `a blank initial balance is valid and cleared once focus is lost`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)
        viewModel.onInitialBalanceChange("  ")

        viewModel.onInitialBalanceFocusLost()

        assertEquals("", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `comments are initially blank`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        assertEquals("", viewModel.state.value.memo)
    }

    @Test
    fun `onMemoChange updates the comments, keeping line breaks`() {
        val viewModel = NewAccountViewModel(fixture.controller, navigator)

        viewModel.onMemoChange("Joint account.\nOpened in 2020.")

        assertEquals("Joint account.\nOpened in 2020.", viewModel.state.value.memo)
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
