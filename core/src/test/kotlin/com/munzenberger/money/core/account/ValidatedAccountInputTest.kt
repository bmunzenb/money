package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidatedAccountInputTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Asset),
        value = AccountTypeConstant.Checking,
    )

    private val accountId = AccountId()

    private val validInput = AccountInput(
        name = "Checking",
        accountType = checking,
        bankName = "",
        bank = null,
        number = "",
        initialBalance = "",
        memo = "",
    )

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

    private fun AccountInput.validAccount(isClosed: Boolean = false) =
        assertIs<ValidatedAccountInput.Valid>(validate(accountId, isClosed))

    @Test
    fun `invalid input reports every error`() {
        val result = validInput.copy(name = "  ", accountType = null, initialBalance = "12abc")
            .validate(accountId, isClosed = false)

        assertEquals(
            ValidatedAccountInput.Invalid(
                setOf(
                    AccountInputError.BlankName,
                    AccountInputError.MissingAccountType,
                    AccountInputError.InvalidInitialBalance,
                )
            ),
            result,
        )
    }

    @Test
    fun `initial balance too large is invalid`() {
        val result = validInput.copy(initialBalance = "999999999999999999999").validate(accountId, isClosed = false)

        assertEquals(ValidatedAccountInput.Invalid(setOf(AccountInputError.InvalidInitialBalance)), result)
    }

    @Test
    fun `account has the given id`() {
        assertEquals(accountId, validInput.validAccount().account.id)
    }

    @Test
    fun `account is open unless closed`() {
        assertFalse(validInput.validAccount(isClosed = false).account.isClosed)
    }

    @Test
    fun `account is closed when closed`() {
        assertTrue(validInput.validAccount(isClosed = true).account.isClosed)
    }

    @Test
    fun `trims values and omits blank optionals`() {
        val (account, newBank) = validInput.copy(name = "  Checking  ", number = "  ", memo = " \n ").validAccount()

        assertEquals("Checking", account.name)
        assertEquals(checking, account.accountType)
        assertNull(account.number)
        assertNull(account.memo)
        assertNull(account.bankId)
        assertEquals(Money(0), account.initialBalance)
        assertNull(newBank)
    }

    @Test
    fun `keeps optional values`() {
        val (account) = validInput.copy(
            number = " 1234-5678 ",
            initialBalance = "-1,234.5",
            memo = "Joint account.\nOpened 2020.",
        ).validAccount()

        assertEquals("1234-5678", account.number)
        assertEquals(Money(-123450), account.initialBalance)
        assertEquals("Joint account.\nOpened 2020.", account.memo)
    }

    @Test
    fun `uses the existing bank`() {
        val bank = Bank(name = "First Bank")

        val (account, newBank) = validInput.copy(bankName = "First Bank", bank = bank).validAccount()

        assertEquals(bank.id, account.bankId)
        assertNull(newBank)
    }

    @Test
    fun `names a new bank for the account`() {
        val (account, newBank) = validInput.copy(bankName = "  New Bank  ").validAccount()

        assertNotNull(newBank)
        assertEquals("New Bank", newBank.name)
        assertEquals(newBank.id, account.bankId)
    }
}
