package com.munzenberger.money.presentation.register

import com.munzenberger.money.core.register.AccountRegister
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.presentation.state.LoadState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegisterBalanceTest {

    private fun account(accountClass: AccountClassConstant, type: AccountTypeConstant) = Account(
        name = type.name,
        accountType = AccountType(
            id = AccountTypeId(type.ordinal.toLong()),
            accountClass = AccountClass(id = AccountClassId(accountClass.ordinal.toLong()), value = accountClass),
            value = type,
        ),
    )

    private val checking = account(AccountClassConstant.Asset, AccountTypeConstant.Checking)
    private val creditCard = account(AccountClassConstant.Liability, AccountTypeConstant.Credit)

    @Test
    fun `a liability shows its balance owed`() {
        assertTrue(creditCard.showsBalanceOwed)
    }

    @Test
    fun `an asset doesn't show its balance owed`() {
        assertFalse(checking.showsBalanceOwed)
    }

    @Test
    fun `a balance owed is negated`() {
        assertEquals(Money(2500), Money(-2500).registerBalance(owed = true))
        assertEquals(Money(-2500), Money(2500).registerBalance(owed = true))
        assertEquals(Money(0), Money(0).registerBalance(owed = true))
    }

    @Test
    fun `a balance that isn't owed keeps its sign`() {
        assertEquals(Money(-2500), Money(-2500).registerBalance(owed = false))
        assertEquals(Money(2500), Money(2500).registerBalance(owed = false))
    }

    @Test
    fun `state's balance isn't loaded until the register loads`() {
        assertEquals(LoadState.Loading, AccountRegisterUiState().showsBalanceOwed)
        assertEquals(LoadState.Loading, AccountRegisterUiState().balance)
        assertEquals(LoadState.Error, AccountRegisterUiState(register = LoadState.Error).showsBalanceOwed)
        assertEquals(LoadState.Error, AccountRegisterUiState(register = LoadState.Error).balance)
    }

    @Test
    fun `state shows a liability's ending balance as the amount owed`() {
        val register = AccountRegister(creditCard, emptyList(), Money(-1200))
        val state = AccountRegisterUiState(register = LoadState.Loaded(register))
        assertEquals(LoadState.Loaded(true), state.showsBalanceOwed)
        assertEquals(LoadState.Loaded(Money(1200)), state.balance)
    }

    @Test
    fun `state keeps an asset's ending balance's sign`() {
        val register = AccountRegister(checking, emptyList(), Money(-1200))
        val state = AccountRegisterUiState(register = LoadState.Loaded(register))
        assertEquals(LoadState.Loaded(false), state.showsBalanceOwed)
        assertEquals(LoadState.Loaded(Money(-1200)), state.balance)
    }
}
