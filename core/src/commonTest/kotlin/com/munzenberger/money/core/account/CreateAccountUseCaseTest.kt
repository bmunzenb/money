package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import java.io.File
import java.util.Locale
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class CreateAccountUseCaseTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

    private val validAccount = NewAccount(
        name = "Checking",
        accountType = checking,
        bankName = "",
        bank = null,
        number = "",
        initialBalance = "",
        memo = "",
    )

    private val writer = mockk<MoneyWriter>(relaxUnitFun = true)
    private val repository = mockk<MoneyRepository> {
        coEvery { transaction<Unit>(any()) } answers { firstArg<MoneyWriter.() -> Unit>().invoke(writer) }
    }

    private val controller = MoneyRepositoryController(
        connectorFactory = { mockk { coEvery { connect() } returns MoneyRepositoryConnectionStatus.Ready(repository) } },
        context = EmptyCoroutineContext,
    )

    private val createAccount = CreateAccountUseCase(controller)

    // The initial balance is parsed for the default locale.
    private val defaultLocale = Locale.getDefault()

    @BeforeTest
    fun setUp() {
        Locale.setDefault(Locale.US)
    }

    @AfterTest
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    private suspend fun connect() {
        controller.openDatabase(File("money-test.db"))
    }

    private fun addedAccount(): Account {
        val account = slot<Account>()
        verify { writer.add(capture(account)) }
        return account.captured
    }

    @Test
    fun testInvalidInputReportsEveryErrorWithoutWriting() = runTest {
        connect()

        val result = createAccount(
            validAccount.copy(name = "  ", accountType = null, initialBalance = "12abc")
        )

        assertEquals(
            CreateAccountResult.Invalid(
                setOf(
                    NewAccountError.BlankName,
                    NewAccountError.MissingAccountType,
                    NewAccountError.InvalidInitialBalance,
                )
            ),
            result,
        )
        coVerify(exactly = 0) { repository.transaction<Any?>(any()) }
    }

    @Test
    fun testInvalidInputIsReportedEvenWithoutAnOpenRepository() = runTest {
        val result = createAccount(validAccount.copy(name = ""))

        assertEquals(CreateAccountResult.Invalid(setOf(NewAccountError.BlankName)), result)
    }

    @Test
    fun testInitialBalanceTooLargeIsInvalid() = runTest {
        connect()

        val result = createAccount(validAccount.copy(initialBalance = "999999999999999999999"))

        assertEquals(CreateAccountResult.Invalid(setOf(NewAccountError.InvalidInitialBalance)), result)
    }

    @Test
    fun testFailsWithoutAnOpenRepository() = runTest {
        val result = createAccount(validAccount)

        assertIs<CreateAccountResult.Failure>(result)
    }

    @Test
    fun testAddsAccountWithTrimmedValuesAndBlankOptionalsOmitted() = runTest {
        connect()

        val result = createAccount(validAccount.copy(name = "  Checking  ", number = "  ", memo = " \n "))

        val account = addedAccount()
        assertEquals(CreateAccountResult.Success(account), result)
        assertEquals("Checking", account.name)
        assertEquals(checking, account.accountType)
        assertNull(account.number)
        assertNull(account.memo)
        assertNull(account.bankId)
        assertEquals(Money(0), account.initialBalance)
        verify(exactly = 0) { writer.add(any<Bank>()) }
    }

    @Test
    fun testAddsAccountWithOptionalValues() = runTest {
        connect()

        createAccount(
            validAccount.copy(number = " 1234-5678 ", initialBalance = "-1,234.5", memo = "Joint account.\nOpened 2020.")
        )

        val account = addedAccount()
        assertEquals("1234-5678", account.number)
        assertEquals(Money(-123450), account.initialBalance)
        assertEquals("Joint account.\nOpened 2020.", account.memo)
    }

    @Test
    fun testUsesTheExistingBank() = runTest {
        connect()
        val bank = Bank(name = "First Bank")

        createAccount(validAccount.copy(bankName = "First Bank", bank = bank))

        assertEquals(bank.id, addedAccount().bankId)
        verify(exactly = 0) { writer.add(any<Bank>()) }
    }

    @Test
    fun testAddsANewBankBeforeTheAccount() = runTest {
        connect()

        createAccount(validAccount.copy(bankName = "  New Bank  "))

        val bank = slot<Bank>()
        val account = slot<Account>()
        verifyOrder {
            writer.add(capture(bank))
            writer.add(capture(account))
        }
        assertEquals("New Bank", bank.captured.name)
        assertEquals(bank.captured.id, account.captured.bankId)
    }

    @Test
    fun testFailsWhenTheTransactionThrows() = runTest {
        val error = IllegalStateException("write failed")
        coEvery { repository.transaction<Unit>(any()) } throws error
        connect()

        val result = createAccount(validAccount)

        assertIs<CreateAccountResult.Failure>(result)
        assertSame(error, result.cause)
    }
}
