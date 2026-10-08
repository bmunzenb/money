package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.CreateAccountResult
import com.munzenberger.money.core.account.CreateAccountUseCase
import com.munzenberger.money.core.account.NewAccount
import com.munzenberger.money.core.account.NewAccountError
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.desktop.MoneyRepositoryControllerFixture
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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
    private val createAccount = mockk<CreateAccountUseCase>()

    private fun viewModel() = NewAccountViewModel(fixture.controller, navigator, createAccount)

    private val assets = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets)
    private val savings = AccountType(id = AccountTypeId(1), accountClass = assets, value = AccountTypeConstant.Savings)
    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = assets,
        value = AccountTypeConstant.Checking,
    )

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
        val viewModel = viewModel()

        assertNull(viewModel.state.value.accountType)
    }

    @Test
    fun `account types are loaded from the connected repository in constant order`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(accountTypes = flowOf(listOf(checking, savings))))

        assertEquals(LoadState.Loaded(listOf(savings, checking)), viewModel.state.value.accountTypes)
    }

    @Test
    fun `account types and banks are loading until a repository is connected`() {
        val viewModel = viewModel()

        assertEquals(LoadState.Loading, viewModel.state.value.accountTypes)
        assertEquals(LoadState.Loading, viewModel.state.value.banks)
    }

    @Test
    fun `account types are an error when they can't be loaded`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(accountTypes = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.accountTypes)
    }

    @Test
    fun `banks are an error when they can't be loaded, and a typed name is still remembered`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(banks = failingFlow()))

        viewModel.onBankNameChange("New Bank")

        assertEquals(LoadState.Error, viewModel.state.value.banks)
        assertEquals("New Bank", viewModel.state.value.bankName)
        assertNull(viewModel.state.value.bank)
    }

    @Test
    fun `onAccountTypeChange selects the account type`() {
        val viewModel = viewModel()

        viewModel.onAccountTypeChange(checking)

        assertEquals(checking, viewModel.state.value.accountType)
    }

    @Test
    fun `onNameChange updates the name`() {
        val viewModel = viewModel()

        viewModel.onNameChange("Checking")

        assertEquals("Checking", viewModel.state.value.name)
    }

    @Test
    fun `banks are loaded from the connected repository sorted by name`() = runTest {
        val viewModel = viewModel()

        fixture.connect(repository(banks = flowOf(listOf(firstBank, creditUnion))))

        assertEquals(LoadState.Loaded(listOf(creditUnion, firstBank)), viewModel.state.value.banks)
    }

    @Test
    fun `financial institution is initially blank`() {
        val viewModel = viewModel()

        assertEquals("", viewModel.state.value.bankName)
        assertNull(viewModel.state.value.bank)
    }

    @Test
    fun `onBankChange selects the existing bank`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(banks = flowOf(listOf(firstBank, creditUnion))))

        viewModel.onBankChange(firstBank)

        assertEquals("First Bank", viewModel.state.value.bankName)
        assertEquals(firstBank, viewModel.state.value.bank)
    }

    @Test
    fun `onBankNameChange with a new name remembers the name without a bank`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(banks = flowOf(listOf(firstBank))))

        viewModel.onBankNameChange("New Bank")

        assertEquals("New Bank", viewModel.state.value.bankName)
        assertNull(viewModel.state.value.bank)
    }

    @Test
    fun `onBankNameChange with an existing bank's name selects that bank`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(banks = flowOf(listOf(firstBank))))

        viewModel.onBankNameChange(" first bank ")

        assertEquals(" first bank ", viewModel.state.value.bankName)
        assertEquals(firstBank, viewModel.state.value.bank)
    }

    @Test
    fun `editing a selected bank's name clears the selection`() = runTest {
        val viewModel = viewModel()
        fixture.connect(repository(banks = flowOf(listOf(firstBank))))
        viewModel.onBankChange(firstBank)

        viewModel.onBankNameChange("First Bank of Detroit")

        assertNull(viewModel.state.value.bank)
    }

    @Test
    fun `account number is initially blank`() {
        val viewModel = viewModel()

        assertEquals("", viewModel.state.value.number)
    }

    @Test
    fun `onNumberChange updates the account number`() {
        val viewModel = viewModel()

        viewModel.onNumberChange("1234-5678")

        assertEquals("1234-5678", viewModel.state.value.number)
    }

    @Test
    fun `initial balance is initially blank without an error`() {
        val viewModel = viewModel()

        assertEquals("", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `currency symbol is the default currency's symbol`() {
        val viewModel = viewModel()

        assertEquals("$", viewModel.state.value.currencySymbol)
    }

    @Test
    fun `an invalid initial balance isn't an error while still typing`() {
        val viewModel = viewModel()

        viewModel.onInitialBalanceChange("-")

        assertEquals("-", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `an invalid initial balance is an error once focus is lost`() {
        val viewModel = viewModel()
        viewModel.onInitialBalanceChange("12abc")

        viewModel.onInitialBalanceFocusLost()

        assertEquals("12abc", viewModel.state.value.initialBalance)
        assertTrue(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `correcting an invalid initial balance clears the error`() {
        val viewModel = viewModel()
        viewModel.onInitialBalanceChange("12abc")
        viewModel.onInitialBalanceFocusLost()

        viewModel.onInitialBalanceChange("12")

        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `a valid initial balance is reformatted once focus is lost`() {
        val viewModel = viewModel()
        viewModel.onInitialBalanceChange("1234.5")

        viewModel.onInitialBalanceFocusLost()

        assertEquals("1,234.50", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `a negative initial balance is valid`() {
        val viewModel = viewModel()
        viewModel.onInitialBalanceChange("-250")

        viewModel.onInitialBalanceFocusLost()

        assertEquals("-250.00", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `a blank initial balance is valid and cleared once focus is lost`() {
        val viewModel = viewModel()
        viewModel.onInitialBalanceChange("  ")

        viewModel.onInitialBalanceFocusLost()

        assertEquals("", viewModel.state.value.initialBalance)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `comments are initially blank`() {
        val viewModel = viewModel()

        assertEquals("", viewModel.state.value.memo)
    }

    @Test
    fun `onMemoChange updates the comments, keeping line breaks`() {
        val viewModel = viewModel()

        viewModel.onMemoChange("Joint account.\nOpened in 2020.")

        assertEquals("Joint account.\nOpened in 2020.", viewModel.state.value.memo)
    }

    @Test
    fun `onBackClick pops the new account route from the back stack`() {
        navigator.navigate { clear(); add(Route.AccountList); add(Route.NewAccount) }
        val viewModel = viewModel()

        viewModel.onBackClick()

        assertEquals(listOf(Route.AccountList), navigator.backStack.toList())
        assertEquals(Route.AccountList, navigator.currentRoute.value)
    }

    @Test
    fun `onSaveClick passes the form's values to the use case`() = runTest {
        coEvery { createAccount(any()) } returns CreateAccountResult.Invalid(emptySet())
        fixture.connect(repository(accountTypes = flowOf(listOf(checking)), banks = flowOf(listOf(firstBank))))
        val viewModel = viewModel()
        viewModel.onNameChange("Checking")
        viewModel.onAccountTypeChange(checking)
        viewModel.onBankChange(firstBank)
        viewModel.onNumberChange("1234")
        viewModel.onInitialBalanceChange("12.50")
        viewModel.onMemoChange("Joint")

        viewModel.onSaveClick()

        coVerify {
            createAccount(
                NewAccount(
                    name = "Checking",
                    accountType = checking,
                    bankName = "First Bank",
                    bank = firstBank,
                    number = "1234",
                    initialBalance = "12.50",
                    memo = "Joint",
                )
            )
        }
    }

    @Test
    fun `invalid fields are marked as errors when saving`() {
        coEvery { createAccount(any()) } returns CreateAccountResult.Invalid(
            setOf(NewAccountError.BlankName, NewAccountError.MissingAccountType, NewAccountError.InvalidInitialBalance)
        )
        val viewModel = viewModel()

        viewModel.onSaveClick()

        val state = viewModel.state.value
        assertTrue(state.isNameError)
        assertTrue(state.isAccountTypeError)
        assertTrue(state.isInitialBalanceError)
        assertEquals(SaveState.Idle, state.saveState)
    }

    @Test
    fun `valid fields are not marked as errors when saving`() {
        coEvery { createAccount(any()) } returns CreateAccountResult.Invalid(setOf(NewAccountError.BlankName))
        val viewModel = viewModel()

        viewModel.onSaveClick()

        assertTrue(viewModel.state.value.isNameError)
        assertFalse(viewModel.state.value.isAccountTypeError)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `entering a name clears the name error`() {
        coEvery { createAccount(any()) } returns CreateAccountResult.Invalid(setOf(NewAccountError.BlankName))
        val viewModel = viewModel()
        viewModel.onSaveClick()

        viewModel.onNameChange(" ")
        assertTrue(viewModel.state.value.isNameError)

        viewModel.onNameChange("Checking")
        assertFalse(viewModel.state.value.isNameError)
    }

    @Test
    fun `picking an account type clears the account type error`() {
        coEvery { createAccount(any()) } returns CreateAccountResult.Invalid(setOf(NewAccountError.MissingAccountType))
        val viewModel = viewModel()
        viewModel.onSaveClick()

        viewModel.onAccountTypeChange(checking)

        assertFalse(viewModel.state.value.isAccountTypeError)
    }

    @Test
    fun `the form is saving until the use case returns`() {
        val result = CompletableDeferred<CreateAccountResult>()
        coEvery { createAccount(any()) } coAnswers { result.await() }
        val viewModel = viewModel()

        viewModel.onSaveClick()
        assertEquals(SaveState.Saving, viewModel.state.value.saveState)

        result.complete(CreateAccountResult.Failure(IllegalStateException("write failed")))
        assertEquals(SaveState.Failed, viewModel.state.value.saveState)
    }

    @Test
    fun `onSaveClick is ignored while already saving`() {
        coEvery { createAccount(any()) } coAnswers { CompletableDeferred<CreateAccountResult>().await() }
        val viewModel = viewModel()

        viewModel.onSaveClick()
        viewModel.onSaveClick()

        coVerify(exactly = 1) { createAccount(any()) }
    }

    @Test
    fun `saving successfully pops the new account route from the back stack`() {
        val account = Account(name = "Checking", accountType = checking)
        coEvery { createAccount(any()) } returns CreateAccountResult.Success(account)
        navigator.navigate { clear(); add(Route.AccountList); add(Route.NewAccount) }
        val viewModel = viewModel()

        viewModel.onSaveClick()

        assertEquals(listOf(Route.AccountList), navigator.backStack.toList())
    }

    @Test
    fun `a failed save can be retried`() {
        coEvery { createAccount(any()) } returns CreateAccountResult.Failure(IllegalStateException("write failed"))
        val viewModel = viewModel()
        viewModel.onSaveClick()
        assertEquals(SaveState.Failed, viewModel.state.value.saveState)

        viewModel.onSaveClick()

        coVerify(exactly = 2) { createAccount(any()) }
    }
}
