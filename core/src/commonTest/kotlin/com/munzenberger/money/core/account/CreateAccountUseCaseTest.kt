package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
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
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** Validating and writing the account is covered by [ValidatedAccountInputTest] and [SaveAccountTest]. */
class CreateAccountUseCaseTest {

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets),
        value = AccountTypeConstant.Checking,
    )

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

    private val createAccount = CreateAccountUseCase(controller)

    private suspend fun connect() {
        controller.openDatabase(File("money-test.db"))
    }

    @Test
    fun testAddsTheAccount() = runTest {
        connect()

        val result = createAccount(validInput)

        val account = slot<Account>()
        verify { writer.add(capture(account)) }
        verify(exactly = 0) { writer.update(any<Account>()) }
        assertEquals(SaveAccountResult.Success(account.captured), result)
    }

    @Test
    fun testEachAccountGetsANewId() = runTest {
        connect()

        createAccount(validInput)
        createAccount(validInput)

        val accounts = mutableListOf<Account>()
        verify(exactly = 2) { writer.add(capture(accounts)) }
        assertNotEquals(accounts[0].id, accounts[1].id)
    }
}
