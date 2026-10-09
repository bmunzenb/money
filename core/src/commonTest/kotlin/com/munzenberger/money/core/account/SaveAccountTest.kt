package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountId
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
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SaveAccountTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
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

    private val writer = mockk<MoneyWriter>(relaxUnitFun = true)
    private val repository = mockk<MoneyRepository> {
        coEvery { transaction<Unit>(any()) } answers { firstArg<MoneyWriter.() -> Unit>().invoke(writer) }
    }

    private val controller = MoneyRepositoryController(
        connectorFactory = { mockk { coEvery { connect() } returns MoneyRepositoryConnectionStatus.Ready(repository) } },
        context = EmptyCoroutineContext,
    )

    /** The accounts passed to the `write` block, in order. */
    private val written = mutableListOf<Account>()

    private suspend fun saveAccount(input: AccountInput) =
        controller.saveAccount(input, accountId) { account ->
            written += account
            update(account)
        }

    private suspend fun connect() {
        controller.openDatabase(File("money-test.db"))
    }

    @Test
    fun testInvalidInputIsReportedWithoutWriting() = runTest {
        connect()

        val result = saveAccount(validInput.copy(name = ""))

        assertEquals(SaveAccountResult.Invalid(setOf(AccountInputError.BlankName)), result)
        coVerify(exactly = 0) { repository.transaction<Any?>(any()) }
        assertTrue(written.isEmpty())
    }

    @Test
    fun testInvalidInputIsReportedEvenWithoutAnOpenRepository() = runTest {
        val result = saveAccount(validInput.copy(name = ""))

        assertEquals(SaveAccountResult.Invalid(setOf(AccountInputError.BlankName)), result)
    }

    @Test
    fun testFailsWithoutAnOpenRepository() = runTest {
        val result = saveAccount(validInput)

        assertIs<SaveAccountResult.Failure>(result)
        assertTrue(written.isEmpty())
    }

    @Test
    fun testWritesTheValidatedAccount() = runTest {
        connect()

        val result = saveAccount(validInput.copy(name = "  Checking  "))

        val account = written.single()
        assertEquals(SaveAccountResult.Success(account), result)
        assertEquals(accountId, account.id)
        assertEquals("Checking", account.name)
        verify { writer.update(account) }
    }

    @Test
    fun testAddsANewBankBeforeWritingTheAccount() = runTest {
        connect()

        saveAccount(validInput.copy(bankName = "New Bank"))

        val bank = slot<Bank>()
        val account = slot<Account>()
        verifyOrder {
            writer.add(capture(bank))
            writer.update(capture(account))
        }
        assertEquals(bank.captured.id, account.captured.bankId)
    }

    @Test
    fun testDoesNotAddAnExistingBank() = runTest {
        connect()

        saveAccount(validInput.copy(bankName = "First Bank", bank = Bank(name = "First Bank")))

        verify(exactly = 0) { writer.add(any<Bank>()) }
    }

    @Test
    fun testFailsWhenTheTransactionThrows() = runTest {
        val error = IllegalStateException("write failed")
        coEvery { repository.transaction<Unit>(any()) } throws error
        connect()

        val result = saveAccount(validInput)

        assertIs<SaveAccountResult.Failure>(result)
        assertSame(error, result.cause)
    }
}
