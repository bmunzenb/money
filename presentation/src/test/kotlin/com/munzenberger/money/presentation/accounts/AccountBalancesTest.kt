package com.munzenberger.money.presentation.accounts

import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.presentation.state.LoadState
import java.util.Currency
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountBalancesTest {

    private val asset = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Asset)
    private val checking = AccountType(
        id = AccountTypeId(1),
        accountClass = asset,
        value = AccountTypeConstant.Checking,
    )
    private val savings = AccountType(id = AccountTypeId(2), accountClass = asset, value = AccountTypeConstant.Savings)

    private val everyday = Account(name = "Everyday", accountType = checking)
    private val bills = Account(name = "Bills", accountType = checking)
    private val rainyDay = Account(name = "Rainy day", accountType = savings)

    private val checkingGroup = AccountGroup.ByAccountType(checking, listOf(bills, everyday))
    private val savingsGroup = AccountGroup.ByAccountType(savings, listOf(rainyDay))
    private val groups = listOf(checkingGroup, savingsGroup)

    @Test
    fun `everything is loading before the balances load`() {
        val balances = accountBalances(groups, null)

        assertEquals(LoadState.Loading, balances.balance(everyday))
        assertEquals(LoadState.Loading, balances.subtotal(checkingGroup))
        assertEquals(LoadState.Loading, balances.subtotal(savingsGroup))
        assertEquals(LoadState.Loading, balances.total)
    }

    @Test
    fun `the subtotals and total are the sums of the balances`() {
        val balances = accountBalances(
            groups,
            Result.success(mapOf(everyday.id to Money(100), bills.id to Money(-250), rainyDay.id to Money(1000))),
        )

        assertEquals(LoadState.Loaded(Money(100)), balances.balance(everyday))
        assertEquals(LoadState.Loaded(Money(-150)), balances.subtotal(checkingGroup))
        assertEquals(LoadState.Loaded(Money(1000)), balances.subtotal(savingsGroup))
        assertEquals(LoadState.Loaded(Money(850)), balances.total)
    }

    @Test
    fun `an account missing from the balances is loading, and so are its group's subtotal and the total`() {
        val balances = accountBalances(
            groups,
            Result.success(mapOf(everyday.id to Money(100), bills.id to Money(250))),
        )

        assertEquals(LoadState.Loading, balances.balance(rainyDay))
        assertEquals(LoadState.Loaded(Money(350)), balances.subtotal(checkingGroup))
        assertEquals(LoadState.Loading, balances.subtotal(savingsGroup))
        assertEquals(LoadState.Loading, balances.total)
    }

    @Test
    fun `failing to load the balances is an error for every balance, subtotal, and the total`() {
        val balances = accountBalances(groups, Result.failure(IllegalStateException("query failed")))

        assertEquals(LoadState.Error, balances.balance(everyday))
        assertEquals(LoadState.Error, balances.balance(rainyDay))
        assertEquals(LoadState.Error, balances.subtotal(checkingGroup))
        assertEquals(LoadState.Error, balances.subtotal(savingsGroup))
        assertEquals(LoadState.Error, balances.total)
    }

    @Test
    fun `a subtotal that overflows is an error`() {
        val balances = accountBalances(
            listOf(checkingGroup),
            Result.success(mapOf(everyday.id to Money(Long.MAX_VALUE), bills.id to Money(1))),
        )

        assertEquals(LoadState.Loaded(Money(Long.MAX_VALUE)), balances.balance(everyday))
        assertEquals(LoadState.Error, balances.subtotal(checkingGroup))
        assertEquals(LoadState.Error, balances.total)
    }

    @Test
    fun `a subtotal that mixes currencies is an error`() {
        val balances = accountBalances(
            listOf(checkingGroup),
            Result.success(mapOf(everyday.id to Money(100), bills.id to Money(100, Currency.getInstance("EUR")))),
        )

        assertEquals(LoadState.Error, balances.subtotal(checkingGroup))
    }

    @Test
    fun `the total of no groups is zero`() {
        assertEquals(LoadState.Loaded(Money(0)), accountBalances(emptyList(), Result.success(emptyMap())).total)
    }
}
