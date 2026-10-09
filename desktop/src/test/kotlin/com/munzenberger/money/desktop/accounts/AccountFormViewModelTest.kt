package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountInput
import com.munzenberger.money.core.account.AccountInputError
import com.munzenberger.money.core.account.CreateAccountUseCase
import com.munzenberger.money.core.account.SaveAccountResult
import com.munzenberger.money.core.account.UpdateAccountUseCase
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
import kotlinx.coroutines.flow.MutableStateFlow
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
class AccountFormViewModelTest {

    private val navigator = Navigator()
    private val fixture = MoneyRepositoryControllerFixture()
    private val createAccount = mockk<CreateAccountUseCase>()
    private val updateAccount = mockk<UpdateAccountUseCase>()

    private fun viewModel(accountId: AccountId? = null) =
        AccountFormViewModel(accountId, fixture.controller, navigator, createAccount, updateAccount)

    private val assets = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets)
    private val savings = AccountType(id = AccountTypeId(1), accountClass = assets, value = AccountTypeConstant.Savings)
    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = assets,
        value = AccountTypeConstant.Checking,
    )

    private val firstBank = Bank(name = "First Bank")
    private val creditUnion = Bank(name = "credit union")

    private val existingAccount = Account(
        name = "Joint Checking",
        number = "1234-5678",
        accountType = checking,
        bankId = firstBank.id,
        initialBalance = Money(125050),
        memo = "Opened 2020.",
    )

    private fun repository(
        accountTypes: Flow<List<AccountType>> = flowOf(emptyList()),
        banks: Flow<List<Bank>> = flowOf(emptyList()),
        accounts: Flow<List<Account>> = flowOf(emptyList()),
    ) = mockk<MoneyRepository>(relaxUnitFun = true) {
        every { this@mockk.accountTypes } returns accountTypes
        every { this@mockk.banks } returns banks
        every { this@mockk.accounts } returns accounts
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
        coEvery { createAccount(any()) } returns SaveAccountResult.Invalid(emptySet())
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
                AccountInput(
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
        coEvery { createAccount(any()) } returns SaveAccountResult.Invalid(
            setOf(
                AccountInputError.BlankName,
                AccountInputError.MissingAccountType,
                AccountInputError.InvalidInitialBalance,
            )
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
        coEvery { createAccount(any()) } returns SaveAccountResult.Invalid(setOf(AccountInputError.BlankName))
        val viewModel = viewModel()

        viewModel.onSaveClick()

        assertTrue(viewModel.state.value.isNameError)
        assertFalse(viewModel.state.value.isAccountTypeError)
        assertFalse(viewModel.state.value.isInitialBalanceError)
    }

    @Test
    fun `entering a name clears the name error`() {
        coEvery { createAccount(any()) } returns SaveAccountResult.Invalid(setOf(AccountInputError.BlankName))
        val viewModel = viewModel()
        viewModel.onSaveClick()

        viewModel.onNameChange(" ")
        assertTrue(viewModel.state.value.isNameError)

        viewModel.onNameChange("Checking")
        assertFalse(viewModel.state.value.isNameError)
    }

    @Test
    fun `picking an account type clears the account type error`() {
        coEvery { createAccount(any()) } returns SaveAccountResult.Invalid(setOf(AccountInputError.MissingAccountType))
        val viewModel = viewModel()
        viewModel.onSaveClick()

        viewModel.onAccountTypeChange(checking)

        assertFalse(viewModel.state.value.isAccountTypeError)
    }

    @Test
    fun `the form is saving until the use case returns`() {
        val result = CompletableDeferred<SaveAccountResult>()
        coEvery { createAccount(any()) } coAnswers { result.await() }
        val viewModel = viewModel()

        viewModel.onSaveClick()
        assertEquals(SaveState.Saving, viewModel.state.value.saveState)

        result.complete(SaveAccountResult.Failure(IllegalStateException("write failed")))
        assertEquals(SaveState.Failed, viewModel.state.value.saveState)
    }

    @Test
    fun `onSaveClick is ignored while already saving`() {
        coEvery { createAccount(any()) } coAnswers { CompletableDeferred<SaveAccountResult>().await() }
        val viewModel = viewModel()

        viewModel.onSaveClick()
        viewModel.onSaveClick()

        coVerify(exactly = 1) { createAccount(any()) }
    }

    @Test
    fun `saving successfully pops the new account route from the back stack`() {
        val account = Account(name = "Checking", accountType = checking)
        coEvery { createAccount(any()) } returns SaveAccountResult.Success(account)
        navigator.navigate { clear(); add(Route.AccountList); add(Route.NewAccount) }
        val viewModel = viewModel()

        viewModel.onSaveClick()

        assertEquals(listOf(Route.AccountList), navigator.backStack.toList())
    }

    @Test
    fun `a failed save can be retried`() {
        coEvery { createAccount(any()) } returns SaveAccountResult.Failure(IllegalStateException("write failed"))
        val viewModel = viewModel()
        viewModel.onSaveClick()
        assertEquals(SaveState.Failed, viewModel.state.value.saveState)

        viewModel.onSaveClick()

        coVerify(exactly = 2) { createAccount(any()) }
    }

    @Test
    fun `a new account isn't editing an existing one`() {
        val viewModel = viewModel()

        assertNull(viewModel.state.value.existingAccount)
        assertFalse(viewModel.state.value.isEditing)
        assertTrue(viewModel.state.value.isFormReady)
    }

    @Test
    fun `an existing account is loading until a repository is connected`() {
        val viewModel = viewModel(existingAccount.id)

        assertEquals(LoadState.Loading, viewModel.state.value.existingAccount)
        assertTrue(viewModel.state.value.isEditing)
        assertFalse(viewModel.state.value.isFormReady)
    }

    @Test
    fun `an existing account fills in the form`() = runTest {
        val viewModel = viewModel(existingAccount.id)

        fixture.connect(
            repository(
                accountTypes = flowOf(listOf(checking)),
                banks = flowOf(listOf(creditUnion, firstBank)),
                accounts = flowOf(listOf(existingAccount)),
            )
        )

        val state = viewModel.state.value
        assertEquals(LoadState.Loaded(existingAccount), state.existingAccount)
        assertTrue(state.isFormReady)
        assertEquals("Joint Checking", state.name)
        assertEquals(checking, state.accountType)
        assertEquals("First Bank", state.bankName)
        assertEquals(firstBank, state.bank)
        assertEquals("1234-5678", state.number)
        assertEquals("1,250.50", state.initialBalance)
        assertEquals("Opened 2020.", state.memo)
    }

    @Test
    fun `an existing account without optional values fills them in blank`() = runTest {
        val account = Account(name = "Cash", accountType = savings)
        val viewModel = viewModel(account.id)

        fixture.connect(repository(accounts = flowOf(listOf(account))))

        val state = viewModel.state.value
        assertEquals("", state.bankName)
        assertNull(state.bank)
        assertEquals("", state.number)
        assertEquals("0.00", state.initialBalance)
        assertEquals("", state.memo)
    }

    @Test
    fun `an existing account is an error when it isn't found`() = runTest {
        val viewModel = viewModel(AccountId())

        fixture.connect(repository(accounts = flowOf(listOf(existingAccount))))

        assertEquals(LoadState.Error, viewModel.state.value.existingAccount)
        assertFalse(viewModel.state.value.isFormReady)
    }

    @Test
    fun `an existing account is an error when the accounts can't be loaded`() = runTest {
        val viewModel = viewModel(existingAccount.id)

        fixture.connect(repository(accounts = failingFlow()))

        assertEquals(LoadState.Error, viewModel.state.value.existingAccount)
    }

    @Test
    fun `an existing account is an error when the banks can't be loaded`() = runTest {
        val viewModel = viewModel(existingAccount.id)

        fixture.connect(repository(banks = failingFlow(), accounts = flowOf(listOf(existingAccount))))

        assertEquals(LoadState.Error, viewModel.state.value.existingAccount)
    }

    @Test
    fun `later changes to an existing account don't overwrite the form`() = runTest {
        val accounts = MutableStateFlow(listOf(existingAccount))
        val viewModel = viewModel(existingAccount.id)
        fixture.connect(repository(banks = flowOf(listOf(firstBank)), accounts = accounts))
        viewModel.onNameChange("Household")

        accounts.value = listOf(existingAccount.copy(name = "Renamed elsewhere"))

        assertEquals("Household", viewModel.state.value.name)
    }

    @Test
    fun `onSaveClick is ignored until an existing account is loaded`() {
        val viewModel = viewModel(existingAccount.id)

        viewModel.onSaveClick()

        assertEquals(SaveState.Idle, viewModel.state.value.saveState)
        coVerify(exactly = 0) { updateAccount(any(), any()) }
    }

    @Test
    fun `saving an existing account updates it with the form's values`() = runTest {
        coEvery { updateAccount(any(), any()) } returns SaveAccountResult.Success(existingAccount)
        navigator.navigate { clear(); add(Route.AccountList); add(Route.EditAccount(existingAccount.id)) }
        val viewModel = viewModel(existingAccount.id)
        fixture.connect(
            repository(
                accountTypes = flowOf(listOf(checking)),
                banks = flowOf(listOf(firstBank)),
                accounts = flowOf(listOf(existingAccount)),
            )
        )
        viewModel.onNameChange("Household")

        viewModel.onSaveClick()

        coVerify {
            updateAccount(
                existingAccount.id,
                AccountInput(
                    name = "Household",
                    accountType = checking,
                    bankName = "First Bank",
                    bank = firstBank,
                    number = "1234-5678",
                    initialBalance = "1,250.50",
                    memo = "Opened 2020.",
                ),
            )
        }
        coVerify(exactly = 0) { createAccount(any()) }
        assertEquals(listOf(Route.AccountList), navigator.backStack.toList())
    }

    @Test
    fun `invalid fields are marked as errors when saving an existing account`() = runTest {
        coEvery { updateAccount(any(), any()) } returns SaveAccountResult.Invalid(setOf(AccountInputError.BlankName))
        val viewModel = viewModel(existingAccount.id)
        fixture.connect(repository(banks = flowOf(listOf(firstBank)), accounts = flowOf(listOf(existingAccount))))
        viewModel.onNameChange("")

        viewModel.onSaveClick()

        assertTrue(viewModel.state.value.isNameError)
        assertEquals(SaveState.Idle, viewModel.state.value.saveState)
    }
}
