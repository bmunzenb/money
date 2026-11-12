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

class AccountRegisterUiStateTest {

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

    private fun loaded(account: Account, endingBalance: Money) =
        AccountRegisterUiState(register = LoadState.Loaded(AccountRegister(account, emptyList(), endingBalance)))

    @Test
    fun `nothing about the account is loaded until the register loads`() {
        val loading = AccountRegisterUiState()
        assertEquals(LoadState.Loading, loading.amountColumns)
        assertEquals(LoadState.Loading, loading.showsBalanceOwed)
        assertEquals(LoadState.Loading, loading.balance)

        val error = AccountRegisterUiState(register = LoadState.Error)
        assertEquals(LoadState.Error, error.amountColumns)
        assertEquals(LoadState.Error, error.showsBalanceOwed)
        assertEquals(LoadState.Error, error.balance)
    }

    @Test
    fun `amount columns follow the loaded account's type`() {
        assertEquals(LoadState.Loaded(RegisterAmountColumns.CreditCard), loaded(creditCard, Money(0)).amountColumns)
    }

    @Test
    fun `a liability's balance is the amount owed`() {
        val state = loaded(creditCard, Money(-1200))
        assertEquals(LoadState.Loaded(true), state.showsBalanceOwed)
        assertEquals(LoadState.Loaded(Money(1200)), state.balance)
    }

    @Test
    fun `an asset's balance keeps its sign`() {
        val state = loaded(checking, Money(-1200))
        assertEquals(LoadState.Loaded(false), state.showsBalanceOwed)
        assertEquals(LoadState.Loaded(Money(-1200)), state.balance)
    }
}
