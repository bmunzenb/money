package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Validating and writing the account is covered by [ValidatedAccountInputTest] and [SaveAccountTest]. */
class UpdateAccountUseCaseTest {

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

    private val updateAccount = UpdateAccountUseCase()

    @Test
    fun testUpdatesTheAccountWithTheGivenId() = runTest {
        val result = updateAccount(repository, accountId, validInput, isClosed = false)

        val account = slot<Account>()
        verify { writer.update(capture(account)) }
        verify(exactly = 0) { writer.add(any<Account>()) }
        assertEquals(accountId, account.captured.id)
        assertEquals(SaveAccountResult.Success(account.captured), result)
    }

    @Test
    fun testUpdatesTheAccountAsOpen() = runTest {
        updateAccount(repository, accountId, validInput, isClosed = false)

        val account = slot<Account>()
        verify { writer.update(capture(account)) }
        assertFalse(account.captured.isClosed)
    }

    @Test
    fun testUpdatesTheAccountAsClosed() = runTest {
        updateAccount(repository, accountId, validInput, isClosed = true)

        val account = slot<Account>()
        verify { writer.update(capture(account)) }
        assertTrue(account.captured.isClosed)
    }
}
